package com.kova700.bookchat.feature.bookshelf.wish.model

import com.kova700.bookchat.core.data.bookshelf.external.model.BookShelfState
import com.kova700.bookchat.core.data.bookshelf.external.model.StarRating
import com.kova700.bookchat.core.data.search.book.external.model.Book
import java.util.Date

sealed interface WishBookShelfItem {

	fun getCategoryId(): Long {
		return when (this) {
			is Header -> HEADER_ITEM_STABLE_ID
			is Item -> bookShelfId
			is Dummy -> hashCode().toLong()
		}
	}

	data class Header(val totalItemCount: Int) : WishBookShelfItem
	data class Item(
		val bookShelfId: Long,
		val book: Book,
		val pages: Int,
		val state: BookShelfState,
		val star: StarRating? = null,
		val lastUpdatedAt: Date,
	) : WishBookShelfItem

	data class Dummy(val id: Int) : WishBookShelfItem

	private companion object {
		private const val HEADER_ITEM_STABLE_ID = -1L
		private const val DUMMY_ITEM_STABLE_ID = -2L
	}
}