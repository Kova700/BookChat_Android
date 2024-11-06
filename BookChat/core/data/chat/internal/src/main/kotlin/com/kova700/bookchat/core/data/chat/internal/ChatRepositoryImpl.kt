package com.kova700.bookchat.core.data.chat.internal

import android.util.Log
import com.kova700.bookchat.core.data.chat.external.model.Chat
import com.kova700.bookchat.core.data.chat.external.model.ChatState
import com.kova700.bookchat.core.data.chat.external.repository.ChatRepository
import com.kova700.bookchat.core.data.common.model.network.BookChatApiResult
import com.kova700.bookchat.core.database.chatting.external.chat.ChatDAO
import com.kova700.bookchat.core.database.chatting.external.chat.mapper.toChat
import com.kova700.bookchat.core.database.chatting.external.chat.mapper.toChatEntity
import com.kova700.bookchat.core.network.bookchat.chat.ChatApi
import com.kova700.bookchat.core.network.bookchat.chat.model.mapper.toChat
import com.kova700.bookchat.core.network.bookchat.common.model.SearchSortOptionNetwork
import com.kova700.bookchat.util.Constants.TAG
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

	private var cachedChannelId: Long? = null

	private var currentOlderChatPage: Long? = null
	private var currentNewerChatPage: Long? = null

	private val _isOlderChatFullyLoaded = MutableStateFlow<Boolean>(false)
	private val _isNewerChatFullyLoaded = MutableStateFlow<Boolean>(true)

	override fun getChannelChatsFlow(
		initFlag: Boolean,
		channelId: Long
	): Flow<List<Chat>> {
		if (initFlag) clearCachedData("getChatsFlow")
		return mapChats.map { it.values }
			.map { chats ->
				chats.filter { chat -> chat.channelId == channelId }
					//ORDER BY state ASC, chat_id DESC
					.sortedWith(compareBy(
						{ chat -> chat.state.code },
						{ chat -> chat.chatId.unaryMinus() }
					))
			}
	}

	override fun getOlderChatIsEndFlow(): StateFlow<Boolean> {
		Log.d(TAG, "ChatRepositoryImpl: getOlderChatIsEndFlow() - called")
		_isOlderChatFullyLoaded.update { false }
		return _isOlderChatFullyLoaded.asStateFlow()
	}

	override fun getNewerChatIsEndFlow(): StateFlow<Boolean> {
		Log.d(TAG, "ChatRepositoryImpl: getNewerChatIsEndFlow() - called")
		_isNewerChatFullyLoaded.update { true }
		return _isNewerChatFullyLoaded.asStateFlow()
	}

	/** 소켓이 끊긴 사이 발생한 채팅들을 서버와 동기화하기 위해서 호출하는 함수
	 * 마지막 load된 채팅을 기준으로 최대 2번 getNewerChats 호출 후, 남은 채팅을 전부 load하지 못했다면,
	 * 가장 최근 채팅만 가져와서 channelLastChat만 갱신하여 유저에게 새로운 채팅이 있음을 알림
	 * (동기화 실패 시, 굳이 유저가 스크롤해서 이동하지 않는 이상, 중간에 load되지 않은 채팅을 남겨둔다는 의미)*/
	override suspend fun syncChats(
		channelId: Long,
		maxAttempts: Int,
	): List<Chat> {
		Log.d(TAG, "ChatRepositoryImpl: syncChats() - called")
		_isNewerChatFullyLoaded.update { false }

		for (i in 0 until 2) {
			runCatching { getNewerChats(channelId) }
			if (_isNewerChatFullyLoaded.value) return emptyList()
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
		Log.d(TAG, "ChatRepositoryImpl: getNewestChats() - called")
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

				clearCachedData("getNewestChats")
				_isOlderChatFullyLoaded.update { newChats.size < size }
				_isNewerChatFullyLoaded.update { true }
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
		clearCachedData("getOfflineNewestChats")
		Log.d(TAG, "ChannelViewModel: getOfflineNewestChats() - called")
		val offlineNewestChats = chatDAO.getNewestChats(
			channelId = channelId,
			size = size
		).map { it.toChat() }
		setChats(mapChats.value + offlineNewestChats.associateBy { it.chatId })
		cachedChannelId = channelId
	}

	override suspend fun getFailedChats(channelId: Long): List<Chat> {
		Log.d(TAG, "ChatRepositoryImpl: getFailedChats() - called")
		return chatDAO.getChannelsFailedChats(channelId).map {
			if (it.isRetryRequired.not()) it.toChat().copy(state = ChatState.FAILURE)
			else it.toChat()
		}
	}

	/** 서버에 있는 채팅 우선적으로 쿼리 + API 실패시 로컬데이터 호출X (채팅 연속성을 보장하지못함)*/
	override suspend fun getChatsAroundId(
		channelId: Long,
		baseChatId: Long,
		size: Int,
	) {
		Log.d(TAG, "ChatRepositoryImpl: getChatsAroundId() - called")
		val response = chatApi.getChats(
			roomId = channelId,
			postCursorId = baseChatId - 1,
			size = size,
			sort = SearchSortOptionNetwork.ID_ASC,
		)

		val newChats = response.chatResponseList.map { it.toChat(channelId = channelId) }

		clearCachedData("getChatsAroundId")
		cachedChannelId = channelId
		_isOlderChatFullyLoaded.update { false }
		_isNewerChatFullyLoaded.update { response.cursorMeta.last }
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
		Log.d(TAG, "ChatRepositoryImpl: getNewerChats() - called")

		val response = chatApi.getChats(
			roomId = channelId,
			postCursorId = currentNewerChatPage,
			size = size,
			sort = SearchSortOptionNetwork.ID_ASC,
		)
		val newChats = response.chatResponseList.map { it.toChat(channelId = channelId) }

		cachedChannelId = channelId
		_isNewerChatFullyLoaded.update { response.cursorMeta.last }
		Log.d(
			TAG,
			"ChatRepositoryImpl: getNewerChats() - response : $response \n" +
							"_isNewerChatFullyLoaded : ${_isNewerChatFullyLoaded.value}"
		)
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
		Log.d(TAG, "ChatRepositoryImpl: getOlderChats() - called")
		val response = chatApi.getChats(
			roomId = channelId,
			postCursorId = currentOlderChatPage,
			size = size,
			sort = SearchSortOptionNetwork.ID_DESC,
		)

		val newChats = response.chatResponseList.map { it.toChat(channelId = channelId) }

		cachedChannelId = channelId
		_isOlderChatFullyLoaded.update { response.cursorMeta.last }
		currentOlderChatPage =
			if (newChats.isEmpty()) currentOlderChatPage else response.cursorMeta.nextCursorId

		/** getNewestChats로 초기화된 경우 삽입 무시 */
		if (currentOlderChatPage == null) return

		insertAllChats(
			channelId = channelId,
			chats = newChats
		)
	}

	/** 로컬에 있는 채팅 우선적으로 쿼리 */
	override suspend fun getChat(chatId: Long): Chat? {
		Log.d(TAG, "ChatRepositoryImpl: getChat() - called")
		return mapChats.value[chatId]
			?: getOfflineChat(chatId)
			?: getOnlineChat(chatId)
	}

	private suspend fun getOfflineChat(chatId: Long): Chat? {
		Log.d(TAG, "ChatRepositoryImpl: getOfflineChat() - called")
		return chatDAO.getChat(chatId)?.toChat()
	}

	private suspend fun getOnlineChat(chatId: Long): Chat? {
		Log.d(TAG, "ChatRepositoryImpl: getOnlineChat() - called")
		return when (val response = chatApi.getChat(chatId)) {
			is BookChatApiResult.Success -> response.data.toChat().also { insertChat(it) }
			is BookChatApiResult.Failure -> null
		}
	}

	override suspend fun insertChat(chat: Chat) {
		Log.d(TAG, "ChatRepositoryImpl: insertChat() - called")
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
		Log.d(TAG, "ChatRepositoryImpl: insertAllChats() - called")
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
		chatState: ChatState,
	): Long {
		Log.d(TAG, "ChatRepositoryImpl: insertWaitingChat() - called")
		val chatId = chatDAO.insertWaitingChat(
			channelId = channelId,
			message = message,
			clientId = clientId,
			chatState = chatState
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
		Log.d(TAG, "ChatRepositoryImpl: updateWaitingChat() - called")
		deleteChat(receiptId)
		chatDAO.insertChat(newChat.toChatEntity())
		setChats(mapChats.value + (newChat.chatId to newChat))
	}

	override suspend fun deleteChat(chatId: Long) {
		Log.d(TAG, "ChatRepositoryImpl: deleteChat() - called")
		chatDAO.deleteChat(chatId)
		setChats(mapChats.value - (chatId))
	}

	override suspend fun updateChatState(chatId: Long, chatState: ChatState) {
		Log.d(TAG, "ChatRepositoryImpl: updateChatState() - called")
		chatDAO.updateChatState(chatId, chatState.code)
	}

	override suspend fun deleteChannelAllChat(channelId: Long) {
		Log.d(TAG, "ChatRepositoryImpl: deleteChannelAllChat() - called")
		chatDAO.deleteChannelAllChat(channelId)
	}

	private fun clearCachedData(caller: String) {
		Log.d(TAG, "ChatRepositoryImpl: clearCachedData() - caller : $caller")
		mapChats.update { emptyMap() }
		cachedChannelId = null
		currentOlderChatPage = null
		currentNewerChatPage = null
		_isOlderChatFullyLoaded.update { false }
		_isNewerChatFullyLoaded.update { true }
	}

	/** 로그아웃 + 회원탈퇴시에 모든 repository 일괄 호출 */
	override suspend fun clear() {
		clearCachedData("clear")
		chatDAO.deleteAll()
	}

	companion object {
		private val DEFAULT_RETRY_ATTEMPT_DELAY_TIME = 1.seconds
	}
}