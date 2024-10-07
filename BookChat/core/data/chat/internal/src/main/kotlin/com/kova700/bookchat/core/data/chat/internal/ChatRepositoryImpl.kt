package com.kova700.bookchat.core.data.chat.internal

import com.kova700.bookchat.core.data.chat.external.model.Chat
import com.kova700.bookchat.core.data.chat.external.model.ChatStatus
import com.kova700.bookchat.core.data.chat.external.repository.ChatRepository
import com.kova700.bookchat.core.database.chatting.external.chat.ChatDAO
import com.kova700.bookchat.core.database.chatting.external.chat.mapper.toChat
import com.kova700.bookchat.core.database.chatting.external.chat.mapper.toChatEntity
import com.kova700.bookchat.core.network.bookchat.chat.ChatApi
import com.kova700.bookchat.core.network.bookchat.chat.model.mapper.toChat
import com.kova700.bookchat.core.network.bookchat.common.model.SearchSortOptionNetwork
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import java.io.IOException
import javax.inject.Inject
import kotlin.math.pow
import kotlin.time.Duration.Companion.seconds

class ChatRepositoryImpl @Inject constructor(
	private val chatApi: ChatApi,
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
	 * 마지막 load된 채팅을 기준으로 최대 2번 getNewerChats 호출 후, 남은 채팅을 전부 load하지 못했다면,
	 * 가장 최근 채팅만 가져와서 channelLastChat만 갱신하여 유저에게 새로운 채팅이 있음을 알림
	 * (유저가 스크롤해서 이동하지 않는 이상, 중간에 load되지 않은 채팅을 남겨둔다는 의미)*/
	override suspend fun syncChats(
		channelId: Long,
		maxAttempts: Int,
	): List<Chat> {
		_isNewerChatFullyLoaded.value = false

		for (i in 0 until 2) {
			runCatching { getNewerChats(channelId) }
			if (_isNewerChatFullyLoaded.value) return sortedChats.firstOrNull() ?: emptyList()
		}

		for (attempt in 0 until maxAttempts) {
			runCatching {
				chatApi.getChats(
					roomId = channelId,
					postCursorId = null,
					size = ChatRepository.CHAT_DEFAULT_LOAD_SIZE,
					sort = SearchSortOptionNetwork.ID_DESC,
				)
			}.onSuccess { response ->
				val newChats = response.chatResponseList.map { it.toChat(channelId = channelId) }
				cachedChannelId = channelId
				chatDAO.insertChats(newChats.toChatEntity())
				return newChats
			}
		}
		throw IOException("chat synchronization is failed")
	}

	/** 서버에 있는 채팅 우선적으로 쿼리 (지수백오프 getNewestChats 요청) */
	override suspend fun getNewestChats(
		channelId: Long,
		size: Int,
		maxAttempts: Int,
	): List<Chat> {
		for (attempt in 0 until maxAttempts) {

			runCatching {
				chatApi.getChats(
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
		throw IOException("failed to retrieve newest chat")
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

		val response = chatApi.getChats(
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

		val response = chatApi.getChats(
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

		val response = chatApi.getChats(
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
		return chatApi.getChat(chatId).toChat()
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