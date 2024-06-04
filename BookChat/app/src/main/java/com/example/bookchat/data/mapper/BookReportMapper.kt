package com.example.bookchat.data.mapper

import com.example.bookchat.domain.model.BookReport
import com.example.bookchat.data.network.model.response.BookReportResponse

fun BookReportResponse.toDomain(): BookReport {
	return BookReport(
		reportTitle = reportTitle,
		reportContent = reportContent,
		reportCreatedAt = reportCreatedAt
	)
}