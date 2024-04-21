package com.example.bookchat.domain.repository

import kotlinx.coroutines.flow.Flow

interface SearchHistoryRepository {
	fun getSearchHistoryFlow(): Flow<List<String>>
	suspend fun addHistory(searchKeyword: String)
	suspend fun removeHistory(index: Int)
	suspend fun clearHistory()
	suspend fun moveHistoryAtTheTop(index: Int)
}