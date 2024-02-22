package com.example.bookchat.domain.repository

import com.example.bookchat.data.SocketMessage
import com.example.bookchat.data.response.RespondGetChat
import com.example.bookchat.utils.SearchSortOption
import kotlinx.coroutines.flow.Flow
import org.hildan.krossbow.stomp.StompSession

interface ChatRepository {
	suspend fun getStompSession(): StompSession

	suspend fun subscribeChatTopic(
		stompSession: StompSession,
		roomSid: String
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
		sort: SearchSortOption = SearchSortOption.ID_DESC,
	): RespondGetChat

}