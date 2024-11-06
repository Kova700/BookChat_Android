package com.kova700.bookchat.core.data.search.channel.external

import com.kova700.bookchat.core.data.search.channel.external.model.ChannelSearchResult
import com.kova700.bookchat.core.data.search.channel.external.model.SearchFilter
import kotlinx.coroutines.flow.Flow

interface ChannelSearchRepository {
	fun getChannelsFLow(initFlag: Boolean = false): Flow<List<ChannelSearchResult>>

	suspend fun search(
		keyword: String,
		searchFilter: SearchFilter,
		initFlag: Boolean = false,
		size: Int = SIMPLE_SEARCH_CHANNELS_LOAD_SIZE,
	): List<ChannelSearchResult>

	fun getCachedChannel(channelId: Long): ChannelSearchResult
	fun clear()

	companion object {
		private const val SIMPLE_SEARCH_CHANNELS_LOAD_SIZE = 10
	}
}