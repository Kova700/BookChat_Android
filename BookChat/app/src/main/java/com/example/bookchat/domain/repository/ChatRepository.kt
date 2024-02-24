package com.example.bookchat.domain.repository

import androidx.paging.PagingData
import com.example.bookchat.data.SocketMessage
import com.example.bookchat.data.database.model.ChatWithUser
import com.example.bookchat.data.response.RespondGetChat
import com.example.bookchat.utils.SearchSortOption
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
	fun getPagedChatFlow(roomId: Long): Flow<PagingData<ChatWithUser>>

	suspend fun connectSocket(roomSid: String, roomId: Long): Flow<SocketMessage>
	suspend fun disconnectSocket()

	suspend fun sendMessage(
		roomId: Long,
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