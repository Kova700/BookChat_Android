package com.example.bookchat.domain.repository

import com.example.bookchat.domain.model.ChatScrap
import kotlinx.coroutines.flow.Flow

interface ChatScrapRepository {

	fun getChatScrapsFlow() : Flow<List<ChatScrap>>
	suspend fun makeChatScrap(
		bookShelfId: Long,
		scrapContent: String, //TODO : 이게 맞나?
	)

	suspend fun getChatScraps(
		bookShelfId: Long,
		postCursorId: Long,
		size: Int,
	)

	suspend fun getChatScrap(scrapId: Long): ChatScrap
	suspend fun deleteChatScrap(scrapId: Long)
}