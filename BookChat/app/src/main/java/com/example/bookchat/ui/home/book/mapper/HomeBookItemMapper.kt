package com.example.bookchat.ui.home.book.mapper

import com.example.bookchat.domain.model.BookShelfItem
import com.example.bookchat.ui.home.book.model.HomeBookItem

fun BookShelfItem.toHomeBookItem(): HomeBookItem {
	return HomeBookItem(
		bookShelfId = bookShelfId,
		book = book,
		state = state,
		lastUpdatedAt = lastUpdatedAt,
	)
}

fun List<BookShelfItem>.toHomeBookItems(): List<HomeBookItem> {
	return map { it.toHomeBookItem() }
}