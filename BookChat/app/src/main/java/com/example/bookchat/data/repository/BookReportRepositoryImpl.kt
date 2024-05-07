package com.example.bookchat.data.repository

import com.example.bookchat.data.mapper.toBookReport
import com.example.bookchat.data.network.BookChatApi
import com.example.bookchat.data.network.model.request.RequestRegisterBookReport
import com.example.bookchat.data.network.model.response.BookReportDoseNotExistException
import com.example.bookchat.data.network.model.response.ResponseBodyEmptyException
import com.example.bookchat.domain.model.BookReport
import com.example.bookchat.domain.repository.BookReportRepository
import javax.inject.Inject

class BookReportRepositoryImpl @Inject constructor(
	private val bookChatApi: BookChatApi
) : BookReportRepository {

	override suspend fun getBookReport(bookShelfId: Long): BookReport {
		val response = bookChatApi.getBookReport(bookShelfId)
		when (response.code()) {
			200 -> {
				val bookReportResult = response.body()
				bookReportResult?.let { return bookReportResult.toBookReport() }
				throw ResponseBodyEmptyException(response.errorBody()?.string())
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
	): BookReport {

		bookChatApi.registerBookReport(
			bookShelfId = bookShelfId,
			requestRegisterBookReport = RequestRegisterBookReport(
				title = reportTitle,
				content = reportContent
			)
		)

		return BookReport(
			reportTitle = reportTitle,
			reportContent = reportContent,
			reportCreatedAt = reportCreatedAt,
		)

	}

	override suspend fun deleteBookReport(bookShelfId: Long) {
		bookChatApi.deleteBookReport(bookShelfId)
	}

	override suspend fun reviseBookReport(
		bookShelfId: Long,
		reportTitle: String,
		reportContent: String,
		reportCreatedAt: String,
	): BookReport {

		bookChatApi.reviseBookReport(
			bookShelfId = bookShelfId,
			requestRegisterBookReport = RequestRegisterBookReport(
				title = reportTitle,
				content = reportContent
			)
		)

		return BookReport(
			reportTitle = reportTitle,
			reportContent = reportContent,
			reportCreatedAt = reportCreatedAt,
		)
	}

	private fun createExceptionMessage(responseCode: Int, responseErrorBody: String?): String {
		return "responseCode : $responseCode , responseErrorBody : $responseErrorBody"
	}
}