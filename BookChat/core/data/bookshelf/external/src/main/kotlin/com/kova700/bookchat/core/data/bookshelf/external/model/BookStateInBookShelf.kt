package com.kova700.bookchat.core.data.bookshelf.external.model

data class BookStateInBookShelf(
	val bookShelfId: Long,
	val bookId: Long,
	val bookShelfState: BookShelfState,
)
