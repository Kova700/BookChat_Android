package com.example.bookchat.ui.bookshelf.model

import com.example.bookchat.domain.model.Book
import com.example.bookchat.domain.model.BookShelfState
import com.example.bookchat.domain.model.StarRating

data class BookShelfListItem(
	val bookShelfId: Long,
	val book: Book,
	val pages: Int,
	val state: BookShelfState,
	val star: StarRating? = null,
	val isSwiped: Boolean = false
){
	companion object{
		val DEFAULT  = BookShelfListItem(
			bookShelfId = 0L,
			book = Book.DEFAULT,
			pages = 0,
			state = BookShelfState.WISH,
		)
	}
}