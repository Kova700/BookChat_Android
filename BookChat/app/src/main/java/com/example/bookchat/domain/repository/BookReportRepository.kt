package com.example.bookchat.domain.repository

import com.example.bookchat.domain.model.BookReport
import kotlinx.coroutines.flow.Flow

interface BookReportRepository {
	fun getBookReportFlow(bookShelfId: Long): Flow<BookReport>
	suspend fun getBookReport(bookShelfId: Long)
	suspend fun registerBookReport(
		bookShelfId: Long,
		reportTitle: String,
		reportContent: String,
		reportCreatedAt: String,
	)

	suspend fun deleteBookReport(bookShelfId: Long)
	suspend fun reviseBookReport(
		bookShelfId: Long,
		reportTitle: String,
		reportContent: String,
		reportCreatedAt: String,
	)
}