package com.example.bookchat.domain.repository

import com.example.bookchat.domain.model.Book
import com.example.bookchat.domain.model.BookSearchSortOption
import com.example.bookchat.utils.BookImgSizeManager
import kotlinx.coroutines.flow.Flow

interface BookSearchRepository {
	fun getBooksFLow(initFlag: Boolean = false): Flow<List<Book>>

	suspend fun search(
		keyword: String,
		sort: BookSearchSortOption = BookSearchSortOption.ACCURACY,
		loadSize: Int = SEARCH_BOOKS_ITEM_LOAD_SIZE * 2
	)

	fun getCachedBook(isbn: String): Book

	companion object {
		val SEARCH_BOOKS_ITEM_LOAD_SIZE = BookImgSizeManager.flexBoxBookSpanSize * 2
	}
}