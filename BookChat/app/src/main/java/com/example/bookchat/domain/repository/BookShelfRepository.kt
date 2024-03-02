package com.example.bookchat.domain.repository

import com.example.bookchat.data.Book
import com.example.bookchat.data.BookShelfItem
import com.example.bookchat.data.request.RequestRegisterBookShelfBook
import com.example.bookchat.data.response.RespondCheckInBookShelf
import com.example.bookchat.data.response.ResponseGetBookShelfBooks
import com.example.bookchat.utils.ReadingStatus
import com.example.bookchat.utils.SearchSortOption

interface BookShelfRepository {
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
}