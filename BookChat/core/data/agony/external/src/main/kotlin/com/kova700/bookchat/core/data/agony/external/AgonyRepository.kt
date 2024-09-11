package com.kova700.bookchat.core.data.agony.external

import com.kova700.bookchat.core.data.agony.external.model.Agony
import com.kova700.bookchat.core.data.agony.external.model.AgonyFolderHexColor
import com.kova700.bookchat.core.data.util.model.SearchSortOption
import kotlinx.coroutines.flow.Flow

interface AgonyRepository {

	fun getAgoniesFlow(initFlag: Boolean = false): Flow<List<Agony>>
	fun getAgonyFlow(agonyId: Long): Flow<Agony>

	suspend fun getAgonies(
		bookShelfId: Long,
		sort: SearchSortOption = SearchSortOption.ID_DESC,
		size: Int = AGONY_LOAD_SIZE,
	)

	suspend fun getAgony(
		bookShelfId: Long,
		agonyId: Long,
	): Agony

	suspend fun makeAgony(
		bookShelfId: Long,
		title: String,
		hexColorCode: AgonyFolderHexColor,
	): Agony

	suspend fun reviseAgony(
		bookShelfId: Long,
		agonyId: Long,
		newTitle: String,
	)

	suspend fun deleteAgony(
		bookShelfId: Long,
		agonyIds: List<Long>,
	)

	fun clear()

	companion object {
		const val AGONY_LOAD_SIZE = 6
	}

}