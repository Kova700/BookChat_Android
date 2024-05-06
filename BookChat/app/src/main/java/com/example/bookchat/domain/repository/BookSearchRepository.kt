package com.example.bookchat.domain.repository

import com.example.bookchat.domain.model.Book
import com.example.bookchat.domain.model.BookSearchSortOption
import kotlinx.coroutines.flow.Flow

interface BookSearchRepository {
	fun getBooksFLow(initFlag: Boolean = false): Flow<List<Book>>

	suspend fun search(
		keyword: String,
		loadSize: Int,
		sort: BookSearchSortOption = BookSearchSortOption.ACCURACY
	): List<Book>

	fun getCachedBook(isbn: String): Book

}