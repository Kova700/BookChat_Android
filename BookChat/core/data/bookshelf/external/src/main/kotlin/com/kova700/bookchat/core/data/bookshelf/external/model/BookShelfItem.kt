package com.kova700.bookchat.core.data.bookshelf.external.model

import com.kova700.bookchat.core.data.search.book.external.model.Book
import java.util.Date

data class BookShelfItem(
	val bookShelfId: Long,
	val book: Book,
	val pages: Int,
	val star: StarRating? = null,
	val state: BookShelfState,
	val lastUpdatedAt: Date,
) {
	companion object {
		val DEFAULT = BookShelfItem(
			bookShelfId = 0L,
			book = Book.DEFAULT,
			pages = 0,
			state = BookShelfState.WISH,
			lastUpdatedAt = Date()
		)
	}
}