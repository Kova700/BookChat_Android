package com.example.bookchat.data.mapper

import com.example.bookchat.data.response.BookStateInBookShelfResponse
import com.example.bookchat.domain.model.BookStateInBookShelf

fun BookStateInBookShelfResponse.toBookStateInBookShelf(): BookStateInBookShelf {
	return BookStateInBookShelf(
		bookShelfId = bookShelfId,
		bookId = bookId,
		bookShelfState = bookShelfState
	)
}