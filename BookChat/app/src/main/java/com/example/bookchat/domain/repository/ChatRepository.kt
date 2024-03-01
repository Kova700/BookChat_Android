package com.example.bookchat.domain.repository

import com.example.bookchat.domain.model.Chat
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
	suspend fun getChatsFlow(channelId :Long): Flow<List<Chat>>

	suspend fun getChats(
		channelId: Long,
		size: Int = CHAT_LOAD_SIZE
	): List<Chat>

	suspend fun insertChat(chat: Chat)
	suspend fun insertWaitingChat(
		roomId: Long,
		message: String,
		myUserId: Long
	): Long

	suspend fun updateWaitingChat(
		newChatId: Long,
		dispatchTime: String,
		status: Int,
		targetChatId: Long,
	)

	suspend fun insertAllChats(chats: List<Chat>)

	companion object {
		const val CHAT_LOAD_SIZE = 30
	}
}