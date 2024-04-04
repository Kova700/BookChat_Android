package com.example.bookchat.domain.repository

import com.example.bookchat.domain.model.AgonyRecord
import com.example.bookchat.domain.model.SearchSortOption
import kotlinx.coroutines.flow.Flow

interface AgonyRecordRepository {

	fun getAgonyRecordsFlow(): Flow<List<AgonyRecord>>

	suspend fun getAgonyRecords(
		bookShelfId: Long,
		agonyId: Long,
		size: Int = AGONY_RECORD_LOAD_SIZE,
		sort: SearchSortOption = SearchSortOption.ID_DESC
	)

	suspend fun makeAgonyRecord(
		bookShelfId: Long,
		agonyId: Long,
		title: String,
		content: String
	)

	suspend fun reviseAgonyRecord(
		bookShelfId: Long,
		agonyId: Long,
		agonyRecord: AgonyRecord,
		newTitle: String,
		newContent: String
	)

	suspend fun deleteAgonyRecord(
		bookShelfId: Long,
		agonyId: Long,
		recordId: Long
	)

	companion object {
		const val AGONY_RECORD_LOAD_SIZE = 4
	}
}