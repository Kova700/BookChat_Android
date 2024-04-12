package com.example.bookchat.domain.model

data class Book(
	val isbn: String,
	var title: String,
	val authors: List<String>,
	val publisher: String,
	var publishAt: String,
	val bookCoverImageUrl: String
) {
	val Book.authorsString: String
		get() = authors.joinToString{""}

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