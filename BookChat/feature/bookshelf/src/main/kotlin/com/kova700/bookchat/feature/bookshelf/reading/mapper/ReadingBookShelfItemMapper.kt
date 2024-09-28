package com.kova700.bookchat.feature.bookshelf.reading.mapper

import com.kova700.bookchat.core.data.bookshelf.external.model.BookShelfItem
import com.kova700.bookchat.feature.bookshelf.reading.ReadingBookShelfUiState
import com.kova700.bookchat.feature.bookshelf.reading.model.ReadingBookShelfItem

fun List<BookShelfItem>.toReadingBookShelfItems(
	totalItemCount: Int,
	isSwipedMap: Map<Long, Boolean>,
	uiState: ReadingBookShelfUiState.UiState,
): List<ReadingBookShelfItem> {
	if (isEmpty()) return emptyList()

	val items = mutableListOf<ReadingBookShelfItem>()
	items.add(ReadingBookShelfItem.Header(totalItemCount))
	items.addAll(map { it.toReadingBookShelfItem(isSwipedMap[it.bookShelfId] ?: false) })
	if (uiState == ReadingBookShelfUiState.UiState.PAGING_ERROR) items.add(ReadingBookShelfItem.PagingRetry)
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