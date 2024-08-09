package com.example.bookchat.data.repository

import com.example.bookchat.data.mapper.toDomain
import com.example.bookchat.data.network.BookChatApi
import com.example.bookchat.data.network.model.request.RequestRegisterBookReport
import com.example.bookchat.data.network.model.response.BookReportDoseNotExistException
import com.example.bookchat.data.network.model.response.ResponseBodyEmptyException
import com.example.bookchat.domain.model.BookReport
import com.example.bookchat.domain.repository.BookReportRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import javax.inject.Inject

class BookReportRepositoryImpl @Inject constructor(
	private val bookChatApi: BookChatApi,
) : BookReportRepository {

	private val bookReports =
		MutableStateFlow<Map<Long, BookReport>>(emptyMap()) //(bookShelfId, BookReport)

	override fun getBookReportFlow(bookShelfId: Long) =
		bookReports.map { it[bookShelfId] }.filterNotNull()

	private fun setBookReports(newBookReports: Map<Long, BookReport>) {
		bookReports.update { newBookReports }
	}

	override suspend fun getBookReport(bookShelfId: Long) {
		val response = bookChatApi.getBookReport(bookShelfId)
		when (response.code()) {
			200 -> {
				val bookReportResult =
					response.body() ?: throw ResponseBodyEmptyException("response body is null")
				val bookReport = bookReportResult.toDomain()
				setBookReports(bookReports.value + (bookShelfId to bookReport))
			}

			404 -> throw BookReportDoseNotExistException()
			else -> throw Exception(
				createExceptionMessage(
					response.code(),
					response.errorBody()?.string()
				)
			)
		}
	}

	override suspend fun registerBookReport(
		bookShelfId: Long,
		reportTitle: String,
		reportContent: String,
		reportCreatedAt: String,
	) {
		bookChatApi.registerBookReport(
			bookShelfId = bookShelfId,
			requestRegisterBookReport = RequestRegisterBookReport(
				title = reportTitle,
				content = reportContent
			)
		)

		setBookReports(
			bookReports.value + (bookShelfId to BookReport(
				reportTitle = reportTitle,
				reportContent = reportContent,
				reportCreatedAt = reportCreatedAt,
			))
		)
	}

	override suspend fun deleteBookReport(bookShelfId: Long) {
		bookChatApi.deleteBookReport(bookShelfId)
		setBookReports(bookReports.value - bookShelfId)
	}

	override suspend fun reviseBookReport(
		bookShelfId: Long,
		reportTitle: String,
		reportContent: String,
		reportCreatedAt: String,
	) {
		bookChatApi.reviseBookReport(
			bookShelfId = bookShelfId,
			requestRegisterBookReport = RequestRegisterBookReport(
				title = reportTitle,
				content = reportContent
			)
		)

		setBookReports(
			bookReports.value + (bookShelfId to BookReport(
				reportTitle = reportTitle,
				reportContent = reportContent,
				reportCreatedAt = reportCreatedAt,
			))
		)
	}

	private fun createExceptionMessage(responseCode: Int, responseErrorBody: String?): String {
		return "responseCode : $responseCode , responseErrorBody : $responseErrorBody"
	}
}