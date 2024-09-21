package com.kova700.bookchat.feature.bookshelf.complete.model

import com.kova700.bookchat.core.data.bookshelf.external.model.BookShelfState
import com.kova700.bookchat.core.data.bookshelf.external.model.StarRating
import com.kova700.bookchat.core.data.search.book.external.model.Book
import java.util.Date

sealed interface CompleteBookShelfItem {

	fun getCategoryId(): Long {
		return when (this) {
			is Header -> HEADER_ITEM_STABLE_ID
			is Item -> bookShelfId
		}
	}

	data class Header(val totalItemCount: Int) : CompleteBookShelfItem
	data class Item(
		val bookShelfId: Long,
		val book: Book,
		val pages: Int,
		val state: BookShelfState,
		val star: StarRating? = null,
		val lastUpdatedAt: Date,
		val isSwiped: Boolean = false,
	) : CompleteBookShelfItem

	private companion object {
		private const val HEADER_ITEM_STABLE_ID = -1L
		private const val DUMMY_ITEM_STABLE_ID = -2L
	}
}