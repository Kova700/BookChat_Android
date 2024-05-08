package com.example.bookchat.data.mapper

import com.example.bookchat.data.network.model.response.BookStateInBookShelfResponse
import com.example.bookchat.domain.model.BookStateInBookShelf

fun com.example.bookchat.data.network.model.response.BookStateInBookShelfResponse.toBookStateInBookShelf(): BookStateInBookShelf {
	return BookStateInBookShelf(
		bookShelfId = bookShelfId,
		bookId = bookId,
		bookShelfState = bookShelfState
	)
}