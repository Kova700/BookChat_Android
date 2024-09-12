package com.kova700.bookchat.core.data.bookshelf.internal.mapper

import com.kova700.bookchat.core.data.bookshelf.external.model.BookStateInBookShelf
import com.kova700.bookchat.core.network.bookchat.model.response.BookStateInBookShelfResponse

fun BookStateInBookShelfResponse.toDomain(): BookStateInBookShelf {
	return BookStateInBookShelf(
		bookShelfId = bookShelfId,
		bookId = bookId,
		bookShelfState = bookShelfState
	)
}