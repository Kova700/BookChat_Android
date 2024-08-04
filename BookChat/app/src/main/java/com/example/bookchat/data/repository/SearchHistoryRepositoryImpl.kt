package com.example.bookchat.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.bookchat.data.datastore.clearData
import com.example.bookchat.data.datastore.getDataFlow
import com.example.bookchat.domain.repository.SearchHistoryRepository
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

class SearchHistoryRepositoryImpl @Inject constructor(
	private val dataStore: DataStore<Preferences>,
	private val gson: Gson,
) : SearchHistoryRepository {
	private val historyKey = stringPreferencesKey(SEARCH_HISTORY_KEY)

	private var cachedHistoryList = mutableListOf<String>()

	override fun getSearchHistoryFlow(): Flow<List<String>> {
		return dataStore.getDataFlow(historyKey)
			.map { historyString ->
				historyString?.let {
					gson.fromJson(historyString, Array<String>::class.java).toMutableList()
				} ?: emptyList()
			}.onEach { cachedHistoryList = it.toMutableList() }
	}

	override suspend fun addHistory(searchKeyword: String) {
		if (cachedHistoryList.firstOrNull() == searchKeyword) return

		if (cachedHistoryList.contains(searchKeyword).not()) {
			cachedHistoryList.add(0, searchKeyword)
		}
		updateDataStore(cachedHistoryList)
	}

	override suspend fun moveHistoryAtTheTop(index: Int) {
		val keyword = cachedHistoryList[index]
		cachedHistoryList.removeAt(index)
		cachedHistoryList.add(0, keyword)
		updateDataStore(cachedHistoryList)
	}

	override suspend fun removeHistory(index: Int) {
		cachedHistoryList.removeAt(index)
		updateDataStore(cachedHistoryList)
	}

	override suspend fun clear() {
		dataStore.clearData(historyKey)
		cachedHistoryList.clear()
	}

	private suspend fun updateDataStore(newList: List<String>) {
		val newHistoryString = gson.toJson(newList)
		dataStore.edit { mutablePreferences ->
			mutablePreferences[historyKey] = newHistoryString
		}
	}

	companion object {
		private const val SEARCH_HISTORY_KEY = "SEARCH_HISTORY_KEY"
	}
}