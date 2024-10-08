package com.kova700.bookchat.feature.bookshelf.wish.mapper

import com.kova700.bookchat.core.data.bookshelf.external.model.BookShelfItem
import com.kova700.bookchat.feature.bookshelf.wish.WishBookShelfUiState
import com.kova700.bookchat.feature.bookshelf.wish.model.WishBookShelfItem

fun List<BookShelfItem>.toWishBookShelfItems(
	totalItemCount: Int,
	dummyItemCount: Int,
	uiState: WishBookShelfUiState.UiState,
): List<WishBookShelfItem> {
	if (isEmpty()) return emptyList()

	val items = mutableListOf<WishBookShelfItem>()
	items.add(WishBookShelfItem.Header(totalItemCount))
	items.addAll(map { it.toWishBookShelfItem() })
	(0 until dummyItemCount).forEach { i -> items.add(WishBookShelfItem.Dummy(i)) }
	if (uiState == WishBookShelfUiState.UiState.PAGING_ERROR) items.add(WishBookShelfItem.PagingRetry)
	return items
}

fun BookShelfItem.toWishBookShelfItem(): WishBookShelfItem.Item {
	return WishBookShelfItem.Item(
		bookShelfId = bookShelfId,
		book = book,
		pages = pages,
		state = state,
		star = star,
		lastUpdatedAt = lastUpdatedAt,
	)
}