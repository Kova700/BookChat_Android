package com.example.bookchat.data.repository

import com.example.bookchat.data.BookReport
import com.example.bookchat.data.network.BookChatApi
import com.example.bookchat.data.network.model.request.RequestRegisterBookReport
import com.example.bookchat.data.network.model.response.BookReportDoseNotExistException
import com.example.bookchat.data.network.model.response.ResponseBodyEmptyException
import com.example.bookchat.domain.model.BookShelfItem
import com.example.bookchat.domain.repository.BookReportRepository
import javax.inject.Inject

class BookReportRepositoryImpl @Inject constructor(
	private val bookChatApi: BookChatApi
) : BookReportRepository {

	override suspend fun getBookReport(book: BookShelfItem): BookReport {
		val response = bookChatApi.getBookReport(book.bookShelfId)
		when (response.code()) {
			200 -> {
				val bookReportResult = response.body()
				bookReportResult?.let { return bookReportResult }
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
		book: BookShelfItem,
		bookReport: BookReport
	) {
		bookChatApi.registerBookReport(
			bookShelfId = book.bookShelfId,
			requestRegisterBookReport = RequestRegisterBookReport(
				bookReport.reportTitle,
				bookReport.reportContent
			)
		)
	}

	override suspend fun deleteBookReport(book: BookShelfItem) {
		bookChatApi.deleteBookReport(book.bookShelfId)
	}

	override suspend fun reviseBookReport(
		book: BookShelfItem,
		bookReport: BookReport
	) {
		bookChatApi.reviseBookReport(
			bookShelfId = book.bookShelfId,
			requestRegisterBookReport = RequestRegisterBookReport(
				bookReport.reportTitle,
				bookReport.reportContent
			)
		)
	}

	private fun createExceptionMessage(responseCode: Int, responseErrorBody: String?): String {
		return "responseCode : $responseCode , responseErrorBody : $responseErrorBody"
	}
}