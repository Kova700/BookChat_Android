package com.example.bookchat.ui.bookshelf.wish.mapper

import com.example.bookchat.domain.model.BookShelfItem
import com.example.bookchat.ui.bookshelf.wish.model.WishBookShelfItem

fun List<BookShelfItem>.toWishBookShelfItems(
	totalItemCount: Int,
	dummyItemCount: Int,
): List<WishBookShelfItem> {
	if (isEmpty()) return emptyList()

	val items = mutableListOf<WishBookShelfItem>()
	items.add(WishBookShelfItem.Header(totalItemCount))
	items.addAll(map { it.toWishBookShelfItem() })
	(0 until dummyItemCount).forEach { i -> items.add(WishBookShelfItem.Dummy(i)) }
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