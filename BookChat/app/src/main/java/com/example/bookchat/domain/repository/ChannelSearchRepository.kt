package com.example.bookchat.domain.repository

import com.example.bookchat.domain.model.Channel
import com.example.bookchat.domain.model.SearchFilter
import kotlinx.coroutines.flow.Flow

interface ChannelSearchRepository {
	fun getChannelsFLow(initFlag: Boolean = false): Flow<List<Channel>>

	suspend fun search(
		keyword: String,
		searchFilter: SearchFilter,
		size: Int = SIMPLE_SEARCH_CHANNELS_LOAD_SIZE,
	): List<Channel>

	fun getCachedChannel(channelId: Long): Channel

	companion object {
		private const val SIMPLE_SEARCH_CHANNELS_LOAD_SIZE = 10
	}

}