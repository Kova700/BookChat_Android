package com.example.bookchat.domain.repository

import com.example.bookchat.data.response.ResponseGetBookSearch
import com.example.bookchat.utils.BookImgSizeManager

interface BookSearchRepository {
	suspend fun searchBooks(
		keyword: String,
		loadSize: Int = SEARCH_BOOKS_ITEM_LOAD_SIZE * 2,
		page: Int = 1
	): ResponseGetBookSearch

	companion object {
		private val SEARCH_BOOKS_ITEM_LOAD_SIZE = BookImgSizeManager.flexBoxBookSpanSize * 2
	}
}