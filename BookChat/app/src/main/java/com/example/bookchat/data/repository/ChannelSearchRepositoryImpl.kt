package com.example.bookchat.data.repository

import com.example.bookchat.data.mapper.toChannelSearchResult
import com.example.bookchat.data.network.BookChatApi
import com.example.bookchat.data.network.model.request.RequestGetSearchedChannels
import com.example.bookchat.domain.model.ChannelSearchResult
import com.example.bookchat.domain.model.SearchFilter
import com.example.bookchat.domain.model.SearchFilter.BOOK_ISBN
import com.example.bookchat.domain.model.SearchFilter.BOOK_TITLE
import com.example.bookchat.domain.model.SearchFilter.ROOM_NAME
import com.example.bookchat.domain.model.SearchFilter.ROOM_TAGS
import com.example.bookchat.domain.repository.ChannelSearchRepository
import com.example.bookchat.domain.repository.ClientRepository
import com.example.bookchat.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import javax.inject.Inject

class ChannelSearchRepositoryImpl @Inject constructor(
	private val bookChatApi: BookChatApi,
	private val clientRepository: ClientRepository,
	private val userRepository: UserRepository,
) : ChannelSearchRepository {

	private val mapChannels =
		MutableStateFlow<Map<Long, ChannelSearchResult>>(emptyMap())//(channelId, Channel)
	private val channels = mapChannels.map { it.values.toList() }.filterNotNull()

	private var cachedSearchKeyword = ""
	private var cachedSearchFilter = BOOK_TITLE
	private var currentPage: Long? = null
	private var isEndPage = false

	override fun getChannelsFLow(initFlag: Boolean): Flow<List<ChannelSearchResult>> {
		if (initFlag) clear()
		return channels
	}

	override suspend fun search(
		keyword: String,
		searchFilter: SearchFilter,
		size: Int,
	): List<ChannelSearchResult> {
		if (cachedSearchKeyword != keyword
			|| cachedSearchFilter != searchFilter
		) clear()
		if (isEndPage) return channels.firstOrNull() ?: emptyList()

		val requestGetSearchedChannels = getRequestGetSearchedChannels(
			keyword = keyword,
			searchFilter = searchFilter
		)

		val response = bookChatApi.getSearchedChannels(
			postCursorId = currentPage,
			size = size,
			roomName = requestGetSearchedChannels.roomName,
			title = requestGetSearchedChannels.title,
			isbn = requestGetSearchedChannels.isbn,
			tags = requestGetSearchedChannels.tags
		)

		cachedSearchKeyword = keyword
		cachedSearchFilter = searchFilter
		isEndPage = response.cursorMeta.last
		currentPage = response.cursorMeta.nextCursorId

		val newChannels = response.channels
			.map {
				it.toChannelSearchResult(
					clientId = clientRepository.getClientProfile().id,
					getUser = { userId -> userRepository.getUser(userId) }
				)
			}
		mapChannels.update { mapChannels.value + newChannels.associateBy { it.roomId } }
		return channels.first()
	}

	private fun getRequestGetSearchedChannels(
		keyword: String,
		searchFilter: SearchFilter,
	): RequestGetSearchedChannels {
		return when (searchFilter) {
			ROOM_NAME -> RequestGetSearchedChannels(roomName = keyword)
			BOOK_TITLE -> RequestGetSearchedChannels(title = keyword)
			BOOK_ISBN -> RequestGetSearchedChannels(isbn = keyword)
			ROOM_TAGS -> RequestGetSearchedChannels(tags = keyword)
		}
	}

	override fun getCachedChannel(channelId: Long): ChannelSearchResult {
		return mapChannels.value[channelId]!!
	}

	override fun clear() {
		mapChannels.update { emptyMap() }
		cachedSearchFilter = BOOK_TITLE
		cachedSearchKeyword = ""
		currentPage = null
		isEndPage = false
	}

}