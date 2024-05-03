package com.example.bookchat.data.repository

import com.example.bookchat.App
import com.example.bookchat.data.mapper.toChannel
import com.example.bookchat.data.network.BookChatApi
import com.example.bookchat.data.network.model.request.RequestGetSearchedChannels
import com.example.bookchat.data.network.model.response.NetworkIsNotConnectedException
import com.example.bookchat.domain.model.Channel
import com.example.bookchat.domain.model.SearchFilter
import com.example.bookchat.domain.model.SearchFilter.BOOK_ISBN
import com.example.bookchat.domain.model.SearchFilter.BOOK_TITLE
import com.example.bookchat.domain.model.SearchFilter.ROOM_NAME
import com.example.bookchat.domain.model.SearchFilter.ROOM_TAGS
import com.example.bookchat.domain.repository.ChannelSearchRepository
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
) : ChannelSearchRepository {

	private val mapChannels = MutableStateFlow<Map<Long, Channel>>(emptyMap())//(channelId, Channel)
	private val channels = mapChannels.map { it.values.toList() }.filterNotNull()

	private var cachedSearchKeyword = ""
	private var currentPage: Long? = null
	private var isEndPage = false

	override fun getChannelsFLow(initFlag: Boolean): Flow<List<Channel>> {
		if (initFlag) clearCachedData()
		return channels
	}

	override suspend fun search(
		keyword: String,
		searchFilter: SearchFilter,
		size: Int
	): List<Channel> {
		if (cachedSearchKeyword != keyword) {
			clearCachedData()
		}
		if (isEndPage) return channels.firstOrNull() ?: emptyList()
		if (isNetworkConnected().not()) throw NetworkIsNotConnectedException()

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
		isEndPage = response.cursorMeta.last
		currentPage = response.cursorMeta.nextCursorId

		val newChannels = response.channels.map { it.toChannel() }
		mapChannels.update { mapChannels.value + newChannels.associateBy { it.roomId } }
		return channels.first()
	}

	override fun getCachedChannel(channelId: Long): Channel {
		return mapChannels.value[channelId]!!
	}

	private fun clearCachedData() {
		mapChannels.update { emptyMap() }
		cachedSearchKeyword = ""
		currentPage = null
		isEndPage = false
	}

	private fun getRequestGetSearchedChannels(
		keyword: String,
		searchFilter: SearchFilter
	): RequestGetSearchedChannels {
		return when (searchFilter) {
			ROOM_NAME -> RequestGetSearchedChannels(roomName = keyword)
			BOOK_TITLE -> RequestGetSearchedChannels(title = keyword)
			BOOK_ISBN -> RequestGetSearchedChannels(isbn = keyword)
			ROOM_TAGS -> RequestGetSearchedChannels(tags = keyword)
		}
	}

	private fun isNetworkConnected(): Boolean {
		return App.instance.isNetworkConnected()
	}
}