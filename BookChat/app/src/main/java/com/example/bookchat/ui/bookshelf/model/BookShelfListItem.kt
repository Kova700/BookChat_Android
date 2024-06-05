package com.example.bookchat.ui.bookshelf.model

import com.example.bookchat.domain.model.Book
import com.example.bookchat.domain.model.BookShelfState
import com.example.bookchat.domain.model.StarRating
import java.util.Date

data class BookShelfListItem(
	val bookShelfId: Long,
	val book: Book,
	val pages: Int,
	val state: BookShelfState,
	val star: StarRating? = null,
	val lastUpdatedAt: Date,
	val isSwiped: Boolean = false,
) {
	companion object {
		val DEFAULT = BookShelfListItem(
			bookShelfId = 0L,
			book = Book.DEFAULT,
			pages = 0,
			lastUpdatedAt = Date(),
			state = BookShelfState.WISH,
		)
	}
}