package com.example.bookchat.domain.repository

import com.example.bookchat.data.Book
import com.example.bookchat.data.BookShelfItem
import com.example.bookchat.data.request.RequestRegisterBookShelfBook
import com.example.bookchat.data.response.RespondCheckInBookShelf
import com.example.bookchat.data.response.ResponseGetBookSearch
import com.example.bookchat.data.response.ResponseGetBookShelfBooks
import com.example.bookchat.utils.BookImgSizeManager
import com.example.bookchat.utils.ReadingStatus
import com.example.bookchat.utils.SearchSortOption

interface BookRepository {
	suspend fun searchBooks(
		keyword: String,
		loadSize: Int = SEARCH_BOOKS_ITEM_LOAD_SIZE * 2,
		page: Int = 1
	): ResponseGetBookSearch

	suspend fun registerBookShelfBook(requestRegisterBookShelfBook: RequestRegisterBookShelfBook)
	suspend fun deleteBookShelfBook(bookId: Long)
	suspend fun changeBookShelfBookStatus(book: BookShelfItem, readingStatus: ReadingStatus)
	suspend fun checkAlreadyInBookShelf(book: Book): RespondCheckInBookShelf?
	suspend fun getBookShelfBooks(
		size: String,
		page: String,
		readingStatus: ReadingStatus,
		sort: SearchSortOption = SearchSortOption.UPDATED_AT_DESC
	): ResponseGetBookShelfBooks

	companion object {
		private val SEARCH_BOOKS_ITEM_LOAD_SIZE = BookImgSizeManager.flexBoxBookSpanSize * 2
	}
}