package com.example.bookchat.domain.repository

import com.example.bookchat.domain.model.BookReport

interface BookReportRepository {
	suspend fun getBookReport(bookShelfId: Long): BookReport
	suspend fun registerBookReport(
		bookShelfId: Long,
		reportTitle: String,
		reportContent: String,
		reportCreatedAt: String,
	): BookReport

	suspend fun deleteBookReport(bookShelfId: Long)
	suspend fun reviseBookReport(
		bookShelfId: Long,
		reportTitle: String,
		reportContent: String,
		reportCreatedAt: String,
	): BookReport
}