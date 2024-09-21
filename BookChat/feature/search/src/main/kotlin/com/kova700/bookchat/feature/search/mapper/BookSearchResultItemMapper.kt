package com.kova700.bookchat.feature.search.mapper

import com.kova700.bookchat.core.data.search.book.external.model.Book
import com.kova700.bookchat.feature.search.model.SearchResultItem

fun Book.toBookItem(): SearchResultItem.BookItem {
	return SearchResultItem.BookItem(
		isbn = isbn,
		title = title,
		authors = authors,
		publisher = publisher,
		publishAt = publishAt,
		bookCoverImageUrl = bookCoverImageUrl
	)
}

fun SearchResultItem.BookItem.toBook(): Book {
	return Book(
		isbn = isbn,
		title = title,
		authors = authors,
		publisher = publisher,
		publishAt = publishAt,
		bookCoverImageUrl = bookCoverImageUrl
	)
}

fun List<Book>.toBookSearchResultItem(flexBoxDummyItemCount: Int): List<SearchResultItem> {
	val groupedItems = mutableListOf<SearchResultItem>()
	groupedItems.addAll(this.map { it.toBookItem() })
	(0 until flexBoxDummyItemCount)
		.forEach { i -> groupedItems.add(SearchResultItem.BookDummy(i)) }
	return groupedItems
}