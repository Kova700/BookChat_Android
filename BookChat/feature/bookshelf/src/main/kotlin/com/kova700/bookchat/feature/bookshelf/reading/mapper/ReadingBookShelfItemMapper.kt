package com.kova700.bookchat.feature.bookshelf.reading.mapper

import com.kova700.bookchat.core.data.bookshelf.external.model.BookShelfItem
import com.kova700.bookchat.feature.bookshelf.reading.model.ReadingBookShelfItem

fun List<BookShelfItem>.toReadingBookShelfItems(
	totalItemCount: Int,
	isSwipedMap: Map<Long, Boolean>,
): List<ReadingBookShelfItem> {
	if (isEmpty()) return emptyList()

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