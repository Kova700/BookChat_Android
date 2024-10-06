package com.kova700.core.data.bookreport.external

import com.kova700.core.data.bookreport.external.model.BookReport
import kotlinx.coroutines.flow.Flow

interface BookReportRepository {
	fun getBookReportFlow(bookShelfId: Long): Flow<BookReport>
	suspend fun getBookReport(bookShelfId: Long)
	suspend fun registerBookReport(
		bookShelfId: Long,
		reportTitle: String,
		reportContent: String,
	)

	suspend fun deleteBookReport(bookShelfId: Long)
	suspend fun reviseBookReport(
		bookShelfId: Long,
		reportTitle: String,
		reportContent: String,
		reportCreatedAt: String,
	)
}