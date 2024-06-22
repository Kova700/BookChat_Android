package com.example.bookchat.domain.repository

import com.example.bookchat.domain.model.ChannelSearchResult
import com.example.bookchat.domain.model.SearchFilter
import kotlinx.coroutines.flow.Flow

interface ChannelSearchRepository {
	fun getChannelsFLow(initFlag: Boolean = false): Flow<List<ChannelSearchResult>>

	suspend fun search(
		keyword: String,
		searchFilter: SearchFilter,
		size: Int = SIMPLE_SEARCH_CHANNELS_LOAD_SIZE,
	): List<ChannelSearchResult>

	fun getCachedChannel(channelId: Long): ChannelSearchResult

	companion object {
		private const val SIMPLE_SEARCH_CHANNELS_LOAD_SIZE = 10
	}

}