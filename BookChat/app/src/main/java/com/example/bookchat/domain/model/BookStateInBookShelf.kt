package com.example.bookchat.domain.model

data class BookStateInBookShelf(
	val bookShelfId: Long,
	val bookId: Long,
	val bookShelfState: BookShelfState,
)
