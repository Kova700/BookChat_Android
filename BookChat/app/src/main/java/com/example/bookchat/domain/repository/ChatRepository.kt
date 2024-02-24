package com.example.bookchat.domain.repository

import androidx.paging.PagingData
import com.example.bookchat.data.SocketMessage
import com.example.bookchat.data.database.model.ChatEntity
import com.example.bookchat.data.database.model.ChatWithUser
import com.example.bookchat.data.response.RespondGetChat
import com.example.bookchat.utils.SearchSortOption
import kotlinx.coroutines.flow.Flow
import org.hildan.krossbow.stomp.StompSession

interface ChatRepository {
	fun getChatDataFlow(roomId: Long): Flow<PagingData<ChatWithUser>>

	suspend fun getStompSession(): StompSession

	suspend fun subscribeChatTopic(
		stompSession: StompSession,
		roomSid: String,
		roomId: Long,
	): Flow<SocketMessage>

	suspend fun sendMessage(
		stompSession: StompSession,
		roomId: Long,
		receiptId: Long,
		message: String
	)

	suspend fun getChat(
		roomId: Long,
		size: Int,
		postCursorId: Long?,
		isFirst: Boolean,
		sort: SearchSortOption = SearchSortOption.ID_DESC,
	): RespondGetChat

	suspend fun getLastChatOfOtherUser(roomId: Long): ChatWithUser
}