package com.kova700.bookchat.core.data.chat.external.repository

import com.kova700.bookchat.core.data.chat.external.model.Chat
import com.kova700.bookchat.core.data.chat.external.model.ChatState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface ChatRepository {
	fun getChannelChatsFlow(
		initFlag: Boolean = false,
		channelId: Long,
	): Flow<List<Chat>>

	suspend fun getOlderChats(
		channelId: Long,
		size: Int = CHAT_DEFAULT_LOAD_SIZE,
	)

	suspend fun getChatsAroundId(
		channelId: Long,
		baseChatId: Long,
		size: Int = CHAT_DEFAULT_LOAD_SIZE,
	)

	suspend fun getNewerChats(
		channelId: Long,
		size: Int = CHAT_DEFAULT_LOAD_SIZE * 2,
	)

	suspend fun getNewestChats(
		channelId: Long,
		size: Int = CHAT_DEFAULT_LOAD_SIZE,
		maxAttempts: Int = DEFAULT_RETRY_MAX_ATTEMPTS,
	): List<Chat>

	suspend fun getOfflineNewestChats(
		channelId: Long,
		size: Int = CHAT_DEFAULT_LOAD_SIZE,
	)

	suspend fun syncChats(
		channelId: Long,
		maxAttempts: Int = DEFAULT_RETRY_MAX_ATTEMPTS,
	): List<Chat>

	suspend fun insertChat(chat: Chat)
	suspend fun insertWaitingChat(
		channelId: Long,
		message: String,
		clientId: Long,
		chatState: ChatState,
	): Long

	suspend fun updateWaitingChat(
		newChat: Chat,
		receiptId: Long,
	)

	suspend fun insertAllChats(
		channelId: Long,
		chats: List<Chat>,
	)

	suspend fun deleteChannelAllChat(channelId: Long)

	suspend fun getChat(chatId: Long): Chat?

	fun getOlderChatIsEndFlow(): StateFlow<Boolean>
	fun getNewerChatIsEndFlow(): StateFlow<Boolean>

	suspend fun getFailedChats(channelId: Long): List<Chat>
	suspend fun deleteChat(chatId: Long)
	suspend fun updateChatState(chatId: Long, chatState: ChatState)
	suspend fun clear()

	companion object {
		const val CHAT_DEFAULT_LOAD_SIZE = 30
		private const val DEFAULT_RETRY_MAX_ATTEMPTS = 5
	}
}