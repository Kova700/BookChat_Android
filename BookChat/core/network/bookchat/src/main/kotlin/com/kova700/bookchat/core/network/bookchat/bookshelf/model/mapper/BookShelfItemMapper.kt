package com.kova700.bookchat.core.network.bookchat.bookshelf.model.mapper

import com.kova700.bookchat.core.data.bookshelf.external.model.BookShelfItem
import com.kova700.bookchat.core.data.bookshelf.external.model.BookShelfState
import com.kova700.bookchat.core.data.search.book.external.model.Book
import com.kova700.bookchat.core.network.bookchat.bookshelf.model.response.BookShelfItemResponse
import com.kova700.bookchat.util.date.toDate
import java.util.Date

fun BookShelfItemResponse.toDomain(state: BookShelfState): BookShelfItem {
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
		state = state,
		lastUpdatedAt = lastUpdatedAt.toDate() ?: Date()
	)
}