package com.example.bookchat.data.repository

import com.example.bookchat.data.mapper.toAgonyRecord
import com.example.bookchat.data.mapper.toNetwork
import com.example.bookchat.data.network.BookChatApi
import com.example.bookchat.domain.model.AgonyRecord
import com.example.bookchat.domain.model.SearchSortOption
import com.example.bookchat.domain.repository.AgonyRecordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import javax.inject.Inject

class AgonyRecordRepositoryImpl @Inject constructor(
	private val bookChatApi: BookChatApi
) : AgonyRecordRepository {

	private val mapAgonyRecords =
		MutableStateFlow<Map<Long, AgonyRecord>>(emptyMap()) //(Id, AgonyRecord)
	private val records = mapAgonyRecords.map {
		it.values.toList().sortedByDescending { record -> record.recordId }
	}

	private var cachedAgonyId: Long = -1
	private var currentPage: Long? = null
	private var isEndPage = false

	override fun getAgonyRecordsFlow(): Flow<List<AgonyRecord>> {
		return records
	}

	override suspend fun getAgonyRecords(
		bookShelfId: Long,
		agonyId: Long,
		size: Int,
		sort: SearchSortOption
	) {
		if (cachedAgonyId != agonyId) {
			clearCachedData()
		}
		if (isEndPage) return

		val response = bookChatApi.getAgonyRecord(
			bookShelfId = bookShelfId,
			agonyId = agonyId,
			postCursorId = currentPage,
			size = size,
			sort = sort.toNetwork()
		)

		cachedAgonyId = agonyId
		isEndPage = response.cursorMeta.last
		currentPage = response.cursorMeta.nextCursorId

		val newRecords = response.agonyRecordResponseList.toAgonyRecord()
		mapAgonyRecords.update { mapAgonyRecords.value + newRecords.associateBy { it.recordId } }
	}

	private fun clearCachedData() {
		mapAgonyRecords.update { emptyMap() }
		cachedAgonyId = -1
		currentPage = null
		isEndPage = false
	}

	//TODO : 생성된 객체 필요
	override suspend fun makeAgonyRecord(
		bookShelfId: Long,
		agonyId: Long,
		title: String,
		content: String
	) {
		val requestMakeAgonyRecord =
			com.example.bookchat.data.network.model.request.RequestMakeAgonyRecord(title, content)
		bookChatApi.makeAgonyRecord(bookShelfId, agonyId, requestMakeAgonyRecord)
	}

	override suspend fun reviseAgonyRecord(
		bookShelfId: Long,
		agonyId: Long,
		agonyRecord: AgonyRecord,
		newTitle: String,
		newContent: String
	) {
		val requestReviseAgonyRecord =
			com.example.bookchat.data.network.model.request.RequestReviseAgonyRecord(newTitle, newContent)
		bookChatApi.reviseAgonyRecord(
			bookShelfId,
			agonyId,
			agonyRecord.recordId,
			requestReviseAgonyRecord
		)
		val newRecord = AgonyRecord(
			recordId = agonyRecord.recordId,
			title = newTitle,
			content = newContent,
			createdAt = agonyRecord.createdAt
		)
		mapAgonyRecords.update { mapAgonyRecords.value + (agonyRecord.recordId to newRecord) }
	}

	override suspend fun deleteAgonyRecord(
		bookShelfId: Long,
		agonyId: Long,
		recordId: Long
	) {
		bookChatApi.deleteAgonyRecord(bookShelfId, agonyId, recordId)
		mapAgonyRecords.update { mapAgonyRecords.value - recordId }
	}

}