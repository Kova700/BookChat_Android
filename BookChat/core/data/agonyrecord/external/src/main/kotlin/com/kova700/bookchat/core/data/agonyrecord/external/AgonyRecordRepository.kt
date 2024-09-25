package com.kova700.bookchat.core.data.agonyrecord.external

import com.kova700.bookchat.core.data.agonyrecord.external.model.AgonyRecord
import com.kova700.bookchat.core.data.common.model.SearchSortOption
import kotlinx.coroutines.flow.Flow

interface AgonyRecordRepository {

	fun getAgonyRecordsFlow(
		initFlag: Boolean = false,
	): Flow<List<AgonyRecord>>

	suspend fun getAgonyRecords(
		bookShelfId: Long,
		agonyId: Long,
		size: Int = AGONY_RECORD_LOAD_SIZE,
		sort: SearchSortOption = SearchSortOption.ID_DESC,
	)

	suspend fun getAgonyRecord(
		bookShelfId: Long,
		agonyId: Long,
		recordId: Long,
	): AgonyRecord

	suspend fun makeAgonyRecord(
		bookShelfId: Long,
		agonyId: Long,
		title: String,
		content: String,
	): AgonyRecord

	suspend fun reviseAgonyRecord(
		bookShelfId: Long,
		agonyId: Long,
		agonyRecord: AgonyRecord,
		newTitle: String,
		newContent: String,
	)

	suspend fun deleteAgonyRecord(
		bookShelfId: Long,
		agonyId: Long,
		recordId: Long,
	)

	fun clear()

	companion object {
		const val AGONY_RECORD_LOAD_SIZE = 4
	}

}