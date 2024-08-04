package com.example.bookchat.domain.repository

import com.example.bookchat.domain.model.Book
import com.example.bookchat.domain.model.BookSearchSortOption
import kotlinx.coroutines.flow.Flow

interface BookSearchRepository {
	fun getBooksFLow(initFlag: Boolean = false): Flow<List<Book>>

	suspend fun search(
		keyword: String,
		loadSize: Int = SEARCH_BOOKS_LOAD_SIZE,
		sort: BookSearchSortOption = BookSearchSortOption.ACCURACY,
	): List<Book>

	fun getCachedBook(isbn: String): Book

	companion object {
		private const val SEARCH_BOOKS_LOAD_SIZE = 30
	}

	fun clear()
}