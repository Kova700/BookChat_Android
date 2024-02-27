package com.example.bookchat.domain.repository

import com.example.bookchat.data.response.RespondGetChat
import com.example.bookchat.domain.model.Chat
import com.example.bookchat.utils.SearchSortOption

interface ChatRepository {
//	fun getPagedChatFlow(roomId: Long): Flow<PagingData<Chat>>

	suspend fun getChat(
		roomId: Long,
		size: Int,
		postCursorId: Long?,
		isFirst: Boolean,
		sort: SearchSortOption = SearchSortOption.ID_DESC,
	): RespondGetChat

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

	suspend fun getLastChatOfOtherUser(roomId: Long): Chat
}