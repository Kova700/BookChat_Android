package com.kova700.bookchat.core.network.bookchat.search.model.book.mapper

import com.kova700.bookchat.core.data.search.book.external.model.Book
import com.kova700.bookchat.core.network.bookchat.search.model.book.response.BookSearchResponse

fun BookSearchResponse.toBook(): Book {
	return Book(
		isbn = isbn,
		title = title,
		authors = authors,
		publisher = publisher,
		publishAt = publishAt,
		bookCoverImageUrl = bookCoverImageUrl
	)
}


fun List<BookSearchResponse>.toBook() = this.map { it.toBook() }