package com.kova700.core.data.searchhistory.internal

import com.kova700.core.data.searchhistory.external.SearchHistoryRepository
import com.kova700.core.datastore.searchhistory.SearchHistoryDataStore
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SearchHistoryRepositoryImpl @Inject constructor(
	private val dataStore: SearchHistoryDataStore,
) : SearchHistoryRepository {

	override fun getSearchHistoryFlow(): Flow<List<String>> {
		return dataStore.getSearchHistoryFlow()
	}

	override suspend fun addHistory(searchKeyword: String) {
		dataStore.addHistory(searchKeyword)
	}

	override suspend fun moveHistoryAtTheTop(index: Int) {
		dataStore.moveHistoryAtTheTop(index)
	}

	override suspend fun removeHistory(index: Int) {
		dataStore.removeHistory(index)
	}

	override suspend fun clear() {
		dataStore.clear()
	}
}