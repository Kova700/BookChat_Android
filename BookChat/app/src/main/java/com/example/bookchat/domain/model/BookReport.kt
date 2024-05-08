package com.example.bookchat.domain.model

data class BookReport(
	val reportTitle: String,
	val reportContent: String,
	val reportCreatedAt: String
) {
	companion object {
		val DEFAULT = BookReport(
			reportTitle = "",
			reportContent = "",
			reportCreatedAt = ""
		)
	}
}
