package com.example.bookchat.ui.bookshelf.reading.mapper

import com.example.bookchat.domain.model.BookShelfItem
import com.example.bookchat.ui.bookshelf.reading.ReadingBookShelfItem

fun List<BookShelfItem>.toReadingBookShelfItems(
	totalItemCount: Int,
	isSwipedMap: Map<Long, Boolean>,
): List<ReadingBookShelfItem> {
	val items = mutableListOf<ReadingBookShelfItem>()
	items.add(ReadingBookShelfItem.Header(totalItemCount))
	items.addAll(map { it.toReadingBookShelfItem(isSwipedMap[it.bookShelfId] ?: false) })
	return items
}

fun BookShelfItem.toReadingBookShelfItem(isSwiped: Boolean): ReadingBookShelfItem.Item {
	return ReadingBookShelfItem.Item(
		bookShelfId = bookShelfId,
		book = book,
		pages = pages,
		state = state,
		star = star,
		lastUpdatedAt = lastUpdatedAt,
		isSwiped = isSwiped
	)
}