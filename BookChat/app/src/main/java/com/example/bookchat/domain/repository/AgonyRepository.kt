package com.example.bookchat.domain.repository

import com.example.bookchat.domain.model.Agony
import com.example.bookchat.domain.model.AgonyFolderHexColor
import com.example.bookchat.domain.model.SearchSortOption
import kotlinx.coroutines.flow.Flow

interface AgonyRepository {

	fun getAgoniesFlow(): Flow<List<Agony>>

	suspend fun getAgonies(
		bookShelfId: Long,
		sort: SearchSortOption = SearchSortOption.ID_DESC,
		size: Int = AGONY_LOAD_SIZE,
	)

	suspend fun makeAgony(
		bookShelfId: Long,
		title: String,
		hexColorCode: AgonyFolderHexColor
	)

	suspend fun reviseAgony(
		bookShelfId: Long,
		agony: Agony,
		newTitle: String
	)

	suspend fun deleteAgony(
		bookShelfId: Long,
		agonyIds: List<Long>
	)

	companion object {
		const val AGONY_LOAD_SIZE = 6
	}
}