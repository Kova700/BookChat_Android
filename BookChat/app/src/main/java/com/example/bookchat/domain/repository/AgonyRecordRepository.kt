package com.example.bookchat.domain.repository

import com.example.bookchat.data.response.ResponseGetAgonyRecord
import com.example.bookchat.domain.model.SearchSortOption

interface AgonyRecordRepository {
	suspend fun makeAgonyRecord(
		bookShelfId: Long,
		agonyId: Long,
		title: String,
		content: String
	)

	suspend fun getAgonyRecord(
		bookShelfId: Long,
		agonyId: Long,
		postCursorId: Long?,
		size: Int,
		sort: SearchSortOption
	): ResponseGetAgonyRecord

	suspend fun reviseAgonyRecord(
		bookShelfId: Long,
		agonyId: Long,
		recordId: Long,
		newTitle: String,
		newContent: String
	)

	suspend fun deleteAgonyRecord(
		bookShelfId: Long,
		agonyId: Long,
		recordId: Long
	)
}