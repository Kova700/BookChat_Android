package com.kova700.bookchat.core.data.agonyrecord.internal

import com.kova700.bookchat.core.data.agonyrecord.external.AgonyRecordRepository
import com.kova700.bookchat.core.data.agonyrecord.external.model.AgonyRecord
import com.kova700.bookchat.core.data.agonyrecord.internal.mapper.toAgonyRecord
import com.kova700.bookchat.core.data.util.mapper.toNetwork
import com.kova700.bookchat.core.data.util.model.SearchSortOption
import com.kova700.bookchat.core.network.bookchat.BookChatApi
import com.kova700.bookchat.core.network.bookchat.model.request.RequestMakeAgonyRecord
import com.kova700.bookchat.core.network.bookchat.model.request.RequestReviseAgonyRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import javax.inject.Inject

class AgonyRecordRepositoryImpl @Inject constructor(
	private val bookChatApi: BookChatApi,
) : AgonyRecordRepository {

	private val mapAgonyRecords =
		MutableStateFlow<Map<Long, AgonyRecord>>(emptyMap()) //(Id, AgonyRecord)
	private val records = mapAgonyRecords.map {
		it.values.toList().sortedByDescending { record -> record.recordId }
	}

	private var cachedAgonyId: Long = -1
	private var currentPage: Long? = null
	private var isEndPage = false

	override fun getAgonyRecordsFlow(initFlag: Boolean): Flow<List<AgonyRecord>> {
		if (initFlag) clear()
		return records
	}

	private fun setAgonyRecords(newAgonyRecords: Map<Long, AgonyRecord>) {
		mapAgonyRecords.update { newAgonyRecords }
	}

	override suspend fun getAgonyRecords(
		bookShelfId: Long,
		agonyId: Long,
		size: Int,
		sort: SearchSortOption,
	) {
		if (cachedAgonyId != agonyId) clear()
		if (isEndPage) return

		val response = bookChatApi.getAgonyRecords(
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
		setAgonyRecords(mapAgonyRecords.value + newRecords.associateBy { it.recordId })
	}

	override suspend fun getAgonyRecord(
		bookShelfId: Long,
		agonyId: Long,
		recordId: Long,
	): AgonyRecord {
		val agonyRecord = mapAgonyRecords.value[recordId]
			?: getOnlineAgonyRecord(
				bookShelfId = bookShelfId,
				agonyId = agonyId,
				recordId = recordId
			)
		setAgonyRecords(mapAgonyRecords.value + (agonyRecord.recordId to agonyRecord))
		return agonyRecord
	}

	private suspend fun getOnlineAgonyRecord(
		bookShelfId: Long,
		agonyId: Long,
		recordId: Long,
	): AgonyRecord {
		return bookChatApi.getAgonyRecord(
			bookShelfId = bookShelfId,
			agonyId = agonyId,
			recordId = recordId
		).toAgonyRecord()
	}

	override suspend fun makeAgonyRecord(
		bookShelfId: Long,
		agonyId: Long,
		title: String,
		content: String,
	): AgonyRecord {
		val requestMakeAgonyRecord = RequestMakeAgonyRecord(title, content)
		val response = bookChatApi.makeAgonyRecord(
			bookShelfId = bookShelfId,
			agonyId = agonyId,
			requestMakeAgonyRecord = requestMakeAgonyRecord
		)

		val createdAgonyRecordId = response.headers()["Location"]
			?.split("/")?.last()?.toLong()
			?: throw Exception("AgonyRecordId does not exist in Http header.")

		return getAgonyRecord(
			bookShelfId = bookShelfId,
			agonyId = agonyId,
			recordId = createdAgonyRecordId
		)
	}

	override suspend fun reviseAgonyRecord(
		bookShelfId: Long,
		agonyId: Long,
		agonyRecord: AgonyRecord,
		newTitle: String,
		newContent: String,
	) {
		val requestReviseAgonyRecord = RequestReviseAgonyRecord(newTitle, newContent)
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
		setAgonyRecords(mapAgonyRecords.value + (agonyRecord.recordId to newRecord))
	}

	override suspend fun deleteAgonyRecord(
		bookShelfId: Long,
		agonyId: Long,
		recordId: Long,
	) {
		bookChatApi.deleteAgonyRecord(bookShelfId, agonyId, recordId)
		setAgonyRecords(mapAgonyRecords.value - recordId)
	}

	override fun clear() {
		setAgonyRecords(emptyMap())
		cachedAgonyId = -1
		currentPage = null
		isEndPage = false
	}

}