package com.example.bookchat.ui.bookshelf.mapper

import com.example.bookchat.domain.model.BookShelfItem
import com.example.bookchat.ui.bookshelf.model.BookShelfListItem

fun BookShelfListItem.toBookShelfItem(): BookShelfItem {
	return BookShelfItem(
		bookShelfId = bookShelfId,
		book = book,
		pages = pages,
		star = star,
		state = state,
		lastUpdatedAt = lastUpdatedAt
	)
}

fun BookShelfItem.toBookShelfListItem(isSwiped: Boolean = false): BookShelfListItem {
	return BookShelfListItem(
		bookShelfId = bookShelfId,
		book = book,
		pages = pages,
		star = star,
		isSwiped = isSwiped,
		state = state,
		lastUpdatedAt = lastUpdatedAt
	)
}

fun List<BookShelfItem>.toBookShelfListItem() = this.map { it.toBookShelfListItem() }