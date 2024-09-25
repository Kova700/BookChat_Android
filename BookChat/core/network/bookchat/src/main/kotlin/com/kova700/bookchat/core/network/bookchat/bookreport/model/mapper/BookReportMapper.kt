package com.kova700.bookchat.core.network.bookchat.bookreport.model.mapper

import com.kova700.bookchat.core.network.bookchat.bookreport.model.response.BookReportResponse
import com.kova700.core.data.bookreport.external.model.BookReport

fun BookReportResponse.toDomain(): BookReport {
	return BookReport(
		reportTitle = reportTitle,
		reportContent = reportContent,
		reportCreatedAt = reportCreatedAt
	)
}