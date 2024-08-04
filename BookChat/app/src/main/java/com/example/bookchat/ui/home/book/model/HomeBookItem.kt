package com.example.bookchat.ui.home.book.model

import com.example.bookchat.domain.model.Book
import com.example.bookchat.domain.model.BookShelfState
import java.util.Date

data class HomeBookItem(
	val bookShelfId: Long,
	val book: Book,
	val state: BookShelfState,
	val lastUpdatedAt: Date,
) {
	companion object {
		val DEFAULT = HomeBookItem(
			bookShelfId = 0L,
			book = Book.DEFAULT,
			state = BookShelfState.READING,
			lastUpdatedAt = Date(),
		)
	}
}