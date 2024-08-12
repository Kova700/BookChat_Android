package com.example.bookchat.data.repository

import com.example.bookchat.data.database.dao.ChatDAO
import com.example.bookchat.data.mapper.toChat
import com.example.bookchat.data.mapper.toChatEntity
import com.example.bookchat.data.network.BookChatApi
import com.example.bookchat.data.network.model.SearchSortOptionNetwork
import com.example.bookchat.domain.model.Chat
import com.example.bookchat.domain.model.ChatStatus
import com.example.bookchat.domain.repository.ChatRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import kotlin.math.pow
import kotlin.time.Duration.Companion.seconds

class ChatRepositoryImpl @Inject constructor(
	private val bookChatApi: BookChatApi,
	private val chatDAO: ChatDAO,
) : ChatRepository {
	private val mapChats =
		MutableStateFlow<Map<Long, Chat>>(emptyMap()) //(chatId, Chat)
	private val sortedChats = mapChats.map { it.values }
		.map { chats ->
			//ORDER BY status ASC, chat_id DESC
			chats.sortedWith(
				compareBy(
					{ chat -> chat.status.code },
					{ chat -> chat.chatId.unaryMinus() }
				))
		}

	private var cachedChannelId: Long? = null

	private var currentOlderChatPage: Long? = null
	private var currentNewerChatPage: Long? = null

	private val _isOlderChatFullyLoaded = MutableStateFlow<Boolean>(false)
	private val _isNewerChatFullyLoaded = MutableStateFlow<Boolean>(true)

	override fun getChatsFlow(
		initFlag: Boolean,
		channelId: Long,
	): Flow<List<Chat>> {
		if (initFlag) clearCachedData()
		return sortedChats
	}

	override fun getOlderChatIsEndFlow(): StateFlow<Boolean> {
		_isOlderChatFullyLoaded.value = false
		return _isOlderChatFullyLoaded.asStateFlow()
	}

	override fun getNewerChatIsEndFlow(): StateFlow<Boolean> {
		_isNewerChatFullyLoaded.value = true
		return _isNewerChatFullyLoaded.asStateFlow()
	}

	/** 소켓이 끊긴 사이 발생한 채팅들을 서버와 동기화하기 위해서 호출하는 함수
	 * 2번 getNewerChats 호출 후, 남은 채팅이 더 있다면
	 * 유저가 아래로 스크롤을 굳이 하지않는 이상. 더 이상 호출하지않고
	 * channelLastChat만 갱신하여 유저에게 새로운 채팅이 있음을 알림 */
	override suspend fun syncChats(
		channelId: Long,
		maxAttempts: Int,
	): List<Chat> {
		_isNewerChatFullyLoaded.value = false

		var isFinishGetNewerChats = false
		for (attempt in 0 until maxAttempts) {

			if (isFinishGetNewerChats.not()) {
				for (i in 0 until 2) {
					runCatching { getNewerChats(channelId) }
					if (_isNewerChatFullyLoaded.value) return sortedChats.firstOrNull() ?: emptyList()
				}
				isFinishGetNewerChats = true
			}

			runCatching {
				bookChatApi.getChats(
					roomId = channelId,
					postCursorId = null,
					size = ChatRepository.CHAT_DEFAULT_LOAD_SIZE,
					sort = SearchSortOptionNetwork.ID_DESC,
				)
			}.onSuccess { response ->
				val newChats = response.chatResponseList
					.map { it.toChat(channelId = channelId) }

				cachedChannelId = channelId
				chatDAO.insertChats(newChats.toChatEntity())
				return newChats
			}
		}
		throw Exception("chat synchronization is failed") //TODO : 필요하다면 커스텀 예외 사용하자
	}

	/** 서버에 있는 채팅 우선적으로 쿼리 (지수백오프 getNewestChats 요청) */
	override suspend fun getNewestChats(
		channelId: Long,
		size: Int,
		maxAttempts: Int,
	): List<Chat> {
		for (attempt in 0 until maxAttempts) {

			runCatching {
				bookChatApi.getChats(
					roomId = channelId,
					postCursorId = null,
					size = size,
					sort = SearchSortOptionNetwork.ID_DESC,
				)
			}.onSuccess { response ->
				val newChats = response.chatResponseList
					.map { it.toChat(channelId = channelId) }

				clearCachedData()
				_isOlderChatFullyLoaded.value = newChats.size < size
				_isNewerChatFullyLoaded.value = true
				cachedChannelId = channelId
				currentOlderChatPage = newChats.lastOrNull()?.chatId
				currentNewerChatPage = newChats.firstOrNull()?.chatId

				insertAllChats(
					channelId = channelId,
					chats = newChats
				)
				return newChats
			}
			delay((DEFAULT_RETRY_ATTEMPT_DELAY_TIME * (1.5).pow(attempt)))
		}
		throw Exception("failed to retrieve newest chat") //TODO : 필요하다면 커스텀 예외 사용하자
	}

	override suspend fun getOfflineNewestChats(
		channelId: Long,
		size: Int,
	) {
		clearCachedData()
		val offlineNewestChats = chatDAO.getNewestChats(
			channelId = channelId,
			size = size
		).map { it.toChat() }
		setChats(mapChats.value + offlineNewestChats.associateBy { it.chatId })
		cachedChannelId = channelId
	}

	//TODO : 추후 WorkManager로 ChatStatus.RETRY_REQUIRED인 채팅들 retry로직 앱 단위로 추가
	override suspend fun getFailedChats(channelId: Long): List<Chat> {
		return chatDAO.getChannelsFailedChats(channelId)
			.map {
				if (it.isRetryRequired.not()) it.toChat().copy(status = ChatStatus.FAILURE)
				else it.toChat()
			}
	}

	/** 서버에 있는 채팅 우선적으로 쿼리 + API 실패시 로컬데이터 호출X (채팅 연속성을 보장하지못함)*/
	override suspend fun getChatsAroundId(
		channelId: Long,
		baseChatId: Long,
		size: Int,
	) {

		val response = bookChatApi.getChats(
			roomId = channelId,
			postCursorId = baseChatId - 1,
			size = size,
			sort = SearchSortOptionNetwork.ID_ASC,
		)

		val newChats = response.chatResponseList.map { it.toChat(channelId = channelId) }

		clearCachedData()
		cachedChannelId = channelId
		_isOlderChatFullyLoaded.value = false
		_isNewerChatFullyLoaded.value = response.cursorMeta.last
		currentOlderChatPage = baseChatId
		currentNewerChatPage =
			if (newChats.isEmpty()) currentNewerChatPage else response.cursorMeta.nextCursorId

		insertAllChats(
			channelId = channelId,
			chats = newChats
		)
	}

	/** 서버에 있는 채팅 우선적으로 쿼리 + API 실패시 로컬데이터 호출X (채팅 연속성을 보장하지못함)*/
	override suspend fun getNewerChats(channelId: Long, size: Int) {
		if (_isNewerChatFullyLoaded.value) return

		val response = bookChatApi.getChats(
			roomId = channelId,
			postCursorId = currentNewerChatPage,
			size = size,
			sort = SearchSortOptionNetwork.ID_ASC,
		)
		val newChats = response.chatResponseList.map { it.toChat(channelId = channelId) }

		cachedChannelId = channelId
		_isNewerChatFullyLoaded.value = response.cursorMeta.last
		currentNewerChatPage =
			if (newChats.isEmpty()) currentNewerChatPage else response.cursorMeta.nextCursorId

		/** getNewestChats로 초기화된 경우 삽입 무시 */
		if (currentNewerChatPage == null) return

		insertAllChats(
			channelId = channelId,
			chats = newChats
		)
	}

	/** 서버에 있는 채팅 우선적으로 쿼리 + API 실패시 로컬데이터 호출X (채팅 연속성을 보장하지못함)*/
	override suspend fun getOlderChats(channelId: Long, size: Int) {
		if (_isOlderChatFullyLoaded.value) return

		val response = bookChatApi.getChats(
			roomId = channelId,
			postCursorId = currentOlderChatPage,
			size = size,
			sort = SearchSortOptionNetwork.ID_DESC,
		)

		val newChats = response.chatResponseList.map { it.toChat(channelId = channelId) }

		cachedChannelId = channelId
		_isOlderChatFullyLoaded.value = response.cursorMeta.last
		currentOlderChatPage =
			if (newChats.isEmpty()) currentNewerChatPage else response.cursorMeta.nextCursorId

		/** getNewestChats로 초기화된 경우 삽입 무시 */
		if (currentOlderChatPage == null) return

		insertAllChats(
			channelId = channelId,
			chats = newChats
		)
	}

	/** 로컬에 있는 채팅 우선적으로 쿼리 */
	override suspend fun getChat(chatId: Long): Chat {
		return mapChats.value[chatId]
			?: getOfflineChat(chatId)
			?: getOnlineChat(chatId)
	}

	private suspend fun getOfflineChat(chatId: Long): Chat? {
		return chatDAO.getChat(chatId)?.toChat()
	}

	private suspend fun getOnlineChat(chatId: Long): Chat {
		return bookChatApi.getChat(chatId).toChat()
			.also { insertChat(it) }
	}

	override suspend fun insertChat(chat: Chat) {
		val chatId = chatDAO.insertChat(chat.toChatEntity())

		if ((chat.channelId != cachedChannelId)
			|| _isNewerChatFullyLoaded.value.not()
		) return

		val newMapChats = mapChats.value + (chatId to chat)
		setChats(newMapChats)
	}

	private fun setChats(newChats: Map<Long, Chat>) {
		mapChats.update { newChats }
	}

	override suspend fun insertAllChats(
		channelId: Long,
		chats: List<Chat>,
	) {
		chatDAO.insertChats(chats.toChatEntity())
		if (channelId != cachedChannelId) return

		if (_isNewerChatFullyLoaded.value.not()) {
			setChats(mapChats.value + chats.associateBy { it.chatId })
			return
		}
		setChats(mapChats.value + (chats + getFailedChats(channelId)).associateBy { it.chatId })
	}

	override suspend fun insertWaitingChat(
		channelId: Long,
		message: String,
		clientId: Long,
		chatStatus: ChatStatus,
	): Long {
		val chatId = chatDAO.insertWaitingChat(
			channelId = channelId,
			message = message,
			clientId = clientId,
			chatStatus = chatStatus
		)
		if (channelId != cachedChannelId
			|| _isNewerChatFullyLoaded.value.not()
		) return chatId

		val newChat = chatDAO.getChat(chatId)?.toChat() ?: return chatId
		setChats(mapChats.value + (chatId to newChat))
		return chatId
	}

	override suspend fun updateWaitingChat(
		newChat: Chat,
		receiptId: Long,
	) {
		deleteChat(receiptId)
		chatDAO.insertChat(newChat.toChatEntity())
		setChats(mapChats.value + (newChat.chatId to newChat))
	}

	override suspend fun deleteChat(chatId: Long) {
		chatDAO.deleteChat(chatId)
		setChats(mapChats.value - (chatId))
	}

	override suspend fun deleteChannelAllChat(channelId: Long) {
		chatDAO.deleteChannelAllChat(channelId)
	}

	private fun clearCachedData() {
		mapChats.update { emptyMap() }
		cachedChannelId = null
		currentOlderChatPage = null
		currentNewerChatPage = null
		_isOlderChatFullyLoaded.value = false
		_isNewerChatFullyLoaded.value = true
	}

	/** 로그아웃 + 회원탈퇴시에 모든 repository 일괄 호출 */
	override suspend fun clear() {
		clearCachedData()
		chatDAO.deleteAll()
	}

	companion object {
		private val DEFAULT_RETRY_ATTEMPT_DELAY_TIME = 1.seconds
	}
}