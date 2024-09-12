package com.kova700.bookchat.core.data.search.book.external

import com.kova700.bookchat.core.data.search.book.external.model.Book
import com.kova700.bookchat.core.data.search.book.external.model.BookSearchSortOption
import kotlinx.coroutines.flow.Flow

interface BookSearchRepository {
	fun getBooksFLow(initFlag: Boolean = false): Flow<List<Book>>

	suspend fun search(
		keyword: String,
		loadSize: Int = SEARCH_BOOKS_LOAD_SIZE,
		sort: BookSearchSortOption = BookSearchSortOption.ACCURACY,
	): List<Book>

	fun getCachedBook(isbn: String): Book
	fun clear()

	companion object {
		private const val SEARCH_BOOKS_LOAD_SIZE = 30
	}
}