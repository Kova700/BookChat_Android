package com.example.bookchat.domain.repository

import com.example.bookchat.data.BookReport
import com.example.bookchat.data.BookShelfItem

interface BookReportRepository {
	suspend fun getBookReport(book: BookShelfItem): BookReport
	suspend fun registerBookReport(
		book: BookShelfItem,
		bookReport: BookReport
	)
	suspend fun deleteBookReport(book: BookShelfItem)
	suspend fun reviseBookReport(
		book: BookShelfItem,
		bookReport: BookReport
	)
}