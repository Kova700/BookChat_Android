package com.kova700.bookchat.feature.bookshelf.reading.model

import com.kova700.bookchat.core.data.bookshelf.external.model.BookShelfState
import com.kova700.bookchat.core.data.bookshelf.external.model.StarRating
import com.kova700.bookchat.core.data.search.book.external.model.Book
import java.util.Date

sealed interface ReadingBookShelfItem {

	fun getCategoryId(): Long {
		return when (this) {
			is Header -> HEADER_ITEM_STABLE_ID
			is Item -> bookShelfId
		}
	}

	data class Header(val totalItemCount: Int) : ReadingBookShelfItem
	data class Item(
		val bookShelfId: Long,
		val book: Book,
		val pages: Int,
		val state: BookShelfState,
		val star: StarRating? = null,
		val lastUpdatedAt: Date,
		val isSwiped: Boolean = false,
	) : ReadingBookShelfItem

	private companion object {
		private const val HEADER_ITEM_STABLE_ID = -1L
	}
}