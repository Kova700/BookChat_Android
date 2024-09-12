package com.kova700.bookchat.core.data.search.book.external.model

data class Book(
	val isbn: String,
	var title: String,
	val authors: List<String>,
	val publisher: String,
	var publishAt: String,
	val bookCoverImageUrl: String
) {
	val authorsString: String
		get() = authors.joinToString(", ")

	companion object {
		val DEFAULT = Book(
			isbn = "",
			title = "",
			authors = emptyList(),
			publisher = "",
			publishAt = "",
			bookCoverImageUrl = ""
		)
	}
}