package com.kova700.core.data.bookreport.internal

import com.kova700.bookchat.core.data.common.model.network.BookChatApiResult
import com.kova700.bookchat.core.network.bookchat.bookreport.BookReportApi
import com.kova700.bookchat.core.network.bookchat.bookreport.model.mapper.toDomain
import com.kova700.bookchat.core.network.bookchat.bookreport.model.request.RequestRegisterBookReport
import com.kova700.core.data.bookreport.external.BookReportRepository
import com.kova700.core.data.bookreport.external.model.BookReport
import com.kova700.core.data.bookreport.external.model.BookReportDoseNotExistException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import java.io.IOException
import javax.inject.Inject

class BookReportRepositoryImpl @Inject constructor(
	private val bookReportApi: BookReportApi,
) : BookReportRepository {

	private val bookReports =
		MutableStateFlow<Map<Long, BookReport>>(emptyMap()) //(bookShelfId, BookReport)

	override fun getBookReportFlow(bookShelfId: Long) =
		bookReports.map { it[bookShelfId] }.filterNotNull()

	private fun setBookReports(newBookReports: Map<Long, BookReport>) {
		bookReports.update { newBookReports }
	}

	override suspend fun getBookReport(bookShelfId: Long) {
		val response = bookReportApi.getBookReport(bookShelfId)
		when (response) {
			is BookChatApiResult.Success -> {
				val bookReport = response.data.toDomain()
				setBookReports(bookReports.value + (bookShelfId to bookReport))
			}

			is BookChatApiResult.Failure -> {
				when (response.code) {
					404 -> throw BookReportDoseNotExistException()
					else -> throw IOException(
						createExceptionMessage(
							responseCode = response.code,
							responseErrorBody = response.body
						)
					)
				}
			}
		}
	}

	override suspend fun registerBookReport(
		bookShelfId: Long,
		reportTitle: String,
		reportContent: String,
	) {
		bookReportApi.registerBookReport(
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
				reportCreatedAt = "",
			))
		)
	}

	override suspend fun deleteBookReport(bookShelfId: Long) {
		bookReportApi.deleteBookReport(bookShelfId)
		setBookReports(bookReports.value - bookShelfId)
	}

	override suspend fun reviseBookReport(
		bookShelfId: Long,
		reportTitle: String,
		reportContent: String,
		reportCreatedAt: String,
	) {
		bookReportApi.reviseBookReport(
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