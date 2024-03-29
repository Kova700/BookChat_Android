package com.example.bookchat.data.mapper

import com.example.bookchat.data.response.BookShelfItemResponse
import com.example.bookchat.domain.model.Book
import com.example.bookchat.domain.model.BookShelfItem
import com.example.bookchat.domain.model.BookShelfState

fun BookShelfItemResponse.toBookShelfItem(
	state: BookShelfState
): BookShelfItem {
	return BookShelfItem(
		bookShelfId = bookShelfId,
		book = Book(
			isbn = isbn,
			title = title,
			authors = authors,
			publisher = publisher,
			publishAt = publishAt,
			bookCoverImageUrl = bookCoverImageUrl
		),
		pages = pages,
		star = star,
		state = state
	)
}