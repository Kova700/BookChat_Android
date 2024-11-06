package com.kova700.bookchat.core.data.search.channel.internal

import com.kova700.bookchat.core.data.search.channel.external.ChannelSearchRepository
import com.kova700.bookchat.core.data.search.channel.external.model.ChannelSearchResult
import com.kova700.bookchat.core.data.search.channel.external.model.SearchFilter
import com.kova700.bookchat.core.data.search.channel.external.model.SearchFilter.BOOK_ISBN
import com.kova700.bookchat.core.data.search.channel.external.model.SearchFilter.BOOK_TITLE
import com.kova700.bookchat.core.data.search.channel.external.model.SearchFilter.ROOM_NAME
import com.kova700.bookchat.core.data.search.channel.external.model.SearchFilter.ROOM_TAGS
import com.kova700.bookchat.core.network.bookchat.search.SearchApi
import com.kova700.bookchat.core.network.bookchat.search.model.channel.mapper.toChannelSearchResult
import com.kova700.bookchat.core.network.bookchat.search.model.channel.request.RequestGetSearchedChannels
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import javax.inject.Inject

class ChannelSearchRepositoryImpl @Inject constructor(
	private val searchApi: SearchApi,
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
		initFlag: Boolean,
		size: Int,
	): List<ChannelSearchResult> {
		if (cachedSearchKeyword != keyword
			|| cachedSearchFilter != searchFilter
			|| initFlag
		) clear()
		if (isEndPage) return channels.firstOrNull() ?: emptyList()

		val requestGetSearchedChannels = getRequestGetSearchedChannels(
			keyword = keyword,
			searchFilter = searchFilter
		)

		val response = searchApi.getSearchedChannels(
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

		val newChannels = response.channels.map { it.toChannelSearchResult() }
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