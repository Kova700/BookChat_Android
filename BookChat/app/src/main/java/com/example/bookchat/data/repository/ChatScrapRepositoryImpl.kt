package com.example.bookchat.data.repository

import com.example.bookchat.data.mapper.toDomain
import com.example.bookchat.data.network.BookChatApi
import com.example.bookchat.data.network.model.request.RequestMakeChatScrap
import com.example.bookchat.domain.model.ChatScrap
import com.example.bookchat.domain.repository.ChatScrapRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import javax.inject.Inject

class ChatScrapRepositoryImpl @Inject constructor(
	private val bookChatApi: BookChatApi,
) : ChatScrapRepository {
	private val mapScraps =
		MutableStateFlow<Map<Long, ChatScrap>>(emptyMap()) //(chatId, Chat)
	private val sortedScraps = mapScraps.map { it.values }
		//ORDER BY scrap_id DESC,
		.map { scraps -> scraps.sortedBy { it.scrapId.unaryMinus() } }

	private fun setScraps(newScraps: Map<Long, ChatScrap>) {
		mapScraps.update { newScraps }
	}

	override fun getChatScrapsFlow(): Flow<List<ChatScrap>> {
		return sortedScraps
	}

	override suspend fun makeChatScrap(
		bookShelfId: Long,
		scrapContent: String, //TODO : 이게 맞나?
	) {
		val request = RequestMakeChatScrap(
			bookShelfId = bookShelfId,
			scrapContent = scrapContent
		)
		val response = bookChatApi.makeChatScrap(request)
		val createdScrapId = response.headers()["Location"]?.split("/")?.last()?.toLong()
			?: throw Exception("scrapId does not exist in Http header.")
		getChatScrap(createdScrapId)
	}

	override suspend fun getChatScraps(
		bookShelfId: Long,
		postCursorId: Long,
		size: Int,
	) {
		val response = bookChatApi.getChatScraps(
			bookShelfId = bookShelfId,
			postCursorId = postCursorId,
			size = size
		)
		response.cursorMeta.last
		response.cursorMeta.nextCursorId
		val newScraps = response.scrapResponseList.map { it.toDomain() }
		setScraps(mapScraps.value + newScraps.associateBy { it.scrapId })
	}

	override suspend fun getChatScrap(scrapId: Long): ChatScrap {
		return mapScraps.value[scrapId]
			?: getOnlineChatsScrap(scrapId)
	}

	private suspend fun getOnlineChatsScrap(scrapId: Long): ChatScrap {
		return bookChatApi.getChatScrap(scrapId).toDomain()
	}

	override suspend fun deleteChatScrap(scrapId: Long) {
		bookChatApi.deleteChatScrap(scrapId)
		setScraps(mapScraps.value - scrapId)
	}
}