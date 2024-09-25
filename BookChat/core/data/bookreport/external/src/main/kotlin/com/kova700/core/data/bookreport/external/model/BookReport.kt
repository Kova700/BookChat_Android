package com.kova700.core.data.bookreport.external.model

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
