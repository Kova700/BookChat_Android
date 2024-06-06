package com.example.bookchat.domain.model

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