package com.kova700.core.data.searchhistory.external

import kotlinx.coroutines.flow.Flow

interface SearchHistoryRepository {
	fun getSearchHistoryFlow(): Flow<List<String>>
	suspend fun addHistory(searchKeyword: String)
	suspend fun removeHistory(index: Int)
	suspend fun clear()
	suspend fun moveHistoryAtTheTop(index: Int)
}