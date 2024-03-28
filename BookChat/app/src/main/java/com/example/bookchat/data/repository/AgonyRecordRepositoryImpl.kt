package com.example.bookchat.data.repository

import com.example.bookchat.App
import com.example.bookchat.data.mapper.toNetwork
import com.example.bookchat.data.network.BookChatApi
import com.example.bookchat.data.request.RequestMakeAgonyRecord
import com.example.bookchat.data.request.RequestReviseAgonyRecord
import com.example.bookchat.data.response.NetworkIsNotConnectedException
import com.example.bookchat.data.response.ResponseGetAgonyRecord
import com.example.bookchat.domain.model.SearchSortOption
import com.example.bookchat.domain.repository.AgonyRecordRepository
import javax.inject.Inject

class AgonyRecordRepositoryImpl @Inject constructor(
	private val bookChatApi: BookChatApi
) : AgonyRecordRepository {

	override suspend fun makeAgonyRecord(
		bookShelfId: Long,
		agonyId: Long,
		title: String,
		content: String
	) {
		if (!isNetworkConnected()) throw NetworkIsNotConnectedException()

		val requestMakeAgonyRecord = RequestMakeAgonyRecord(title, content)
		val response = bookChatApi.makeAgonyRecord(bookShelfId, agonyId, requestMakeAgonyRecord)
		when (response.code()) {
			200 -> {}
			else -> throw Exception(
				createExceptionMessage(
					response.code(),
					response.errorBody()?.string()
				)
			)
		}
	}

	override suspend fun getAgonyRecord(
		bookShelfId: Long,
		agonyId: Long,
		postCursorId: Long?,
		size: Int,
		sort: SearchSortOption
	): ResponseGetAgonyRecord {
		if (!isNetworkConnected()) throw NetworkIsNotConnectedException()

		return bookChatApi.getAgonyRecord(
			bookShelfId = bookShelfId,
			agonyId = agonyId,
			postCursorId = postCursorId,
			size = size,
			sort = sort.toNetwork()
		)
	}

	override suspend fun reviseAgonyRecord(
		bookShelfId: Long,
		agonyId: Long,
		recordId: Long,
		newTitle: String,
		newContent: String
	) {
		if (!isNetworkConnected()) throw NetworkIsNotConnectedException()

		val requestReviseAgonyRecord = RequestReviseAgonyRecord(newTitle, newContent)
		val response = bookChatApi.reviseAgonyRecord(
			bookShelfId,
			agonyId,
			recordId,
			requestReviseAgonyRecord
		)
		when (response.code()) {
			200 -> {}
			else -> throw Exception(
				createExceptionMessage(
					response.code(),
					response.errorBody()?.string()
				)
			)
		}
	}

	override suspend fun deleteAgonyRecord(
		bookShelfId: Long,
		agonyId: Long,
		recordId: Long
	) {
		if (!isNetworkConnected()) throw NetworkIsNotConnectedException()

		val response = bookChatApi.deleteAgonyRecord(bookShelfId, agonyId, recordId)
		when (response.code()) {
			200 -> {}
			else -> throw Exception(
				createExceptionMessage(
					response.code(),
					response.errorBody()?.string()
				)
			)
		}
	}

	private fun isNetworkConnected(): Boolean {
		return App.instance.isNetworkConnected()
	}

	private fun createExceptionMessage(responseCode: Int, responseErrorBody: String?): String {
		return "responseCode : $responseCode , responseErrorBody : $responseErrorBody"
	}
}