package com.kova700.bookchat.feature.bookshelf.complete.mapper

import com.kova700.bookchat.core.data.bookshelf.external.model.BookShelfItem
import com.kova700.bookchat.feature.bookshelf.complete.model.CompleteBookShelfItem

fun List<BookShelfItem>.toCompleteBookShelfItems(
	totalItemCount: Int,
	isSwipedMap: Map<Long, Boolean>,
): List<CompleteBookShelfItem> {
	if (isEmpty()) return emptyList()

	val items = mutableListOf<CompleteBookShelfItem>()
	items.add(CompleteBookShelfItem.Header(totalItemCount))
	items.addAll(map { it.toCompleteBookShelfItem(isSwipedMap[it.bookShelfId] ?: false) })
	return items
}

fun BookShelfItem.toCompleteBookShelfItem(isSwiped: Boolean): CompleteBookShelfItem.Item {
	return CompleteBookShelfItem.Item(
		bookShelfId = bookShelfId,
		book = book,
		pages = pages,
		state = state,
		star = star,
		lastUpdatedAt = lastUpdatedAt,
		isSwiped = isSwiped
	)
}