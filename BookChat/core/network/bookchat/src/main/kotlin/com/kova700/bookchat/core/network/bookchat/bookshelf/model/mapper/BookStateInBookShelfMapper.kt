package com.kova700.bookchat.core.network.bookchat.bookshelf.model.mapper

import com.kova700.bookchat.core.data.bookshelf.external.model.BookStateInBookShelf
import com.kova700.bookchat.core.network.bookchat.bookshelf.model.response.BookStateInBookShelfResponse

fun BookStateInBookShelfResponse.toDomain(): BookStateInBookShelf {
	return BookStateInBookShelf(
		bookShelfId = bookShelfId,
		bookId = bookId,
		bookShelfState = bookShelfState
	)
}