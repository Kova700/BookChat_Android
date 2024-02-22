package com.example.bookchat.data.repository

import com.example.bookchat.App
import com.example.bookchat.data.Book
import com.example.bookchat.data.BookShelfItem
import com.example.bookchat.data.api.BookChatApi
import com.example.bookchat.data.request.RequestChangeBookStatus
import com.example.bookchat.data.request.RequestRegisterBookShelfBook
import com.example.bookchat.data.response.NetworkIsNotConnectedException
import com.example.bookchat.data.response.RespondCheckInBookShelf
import com.example.bookchat.data.response.ResponseBodyEmptyException
import com.example.bookchat.data.response.ResponseGetBookSearch
import com.example.bookchat.data.response.ResponseGetBookShelfBooks
import com.example.bookchat.domain.repository.BookRepository
import com.example.bookchat.utils.BookImgSizeManager
import com.example.bookchat.utils.ReadingStatus
import com.example.bookchat.utils.SearchSortOption
import javax.inject.Inject

class BookRepositoryImpl @Inject constructor(
	private val bookChatApi: BookChatApi
) : BookRepository {

	override suspend fun searchBooks(
		keyword: String,
		loadSize: Int,
		page :Int
	): ResponseGetBookSearch {
		if (!isNetworkConnected()) throw NetworkIsNotConnectedException()

		return bookChatApi.getBookSearchResult(
			query = keyword,
			size = loadSize.toString(),
			page = page.toString()
		)
	}

	override suspend fun registerBookShelfBook(requestRegisterBookShelfBook: RequestRegisterBookShelfBook) {
		if (!isNetworkConnected()) throw NetworkIsNotConnectedException()
		val response = bookChatApi.registerBookShelfBook(requestRegisterBookShelfBook)
		when (response.code()) {
			200 -> {}
			else -> throw Exception(
				createExceptionMessage(
					response.code(),
					response.errorBody()?.string()
				)
			)
		}
	}

	override suspend fun deleteBookShelfBook(bookId: Long) {
		if (!isNetworkConnected()) throw NetworkIsNotConnectedException()

		val response = bookChatApi.deleteBookShelfBook(bookId)
		when (response.code()) {
			200 -> {}
			else -> throw Exception(
				createExceptionMessage(
					response.code(),
					response.errorBody()?.string()
				)
			)
		}
	}

	override suspend fun changeBookShelfBookStatus(
		book: BookShelfItem,
		readingStatus: ReadingStatus
	) {
		if (!isNetworkConnected()) throw NetworkIsNotConnectedException()

		val requestBody = RequestChangeBookStatus(
			readingStatus = readingStatus,
			star = book.star,
			pages = book.pages
		)
		val response = bookChatApi.changeBookShelfBookStatus(book.bookShelfId, requestBody)
		when (response.code()) {
			200 -> {}
			else -> throw Exception(
				createExceptionMessage(
					response.code(),
					response.errorBody()?.string()
				)
			)
		}
	}

	override suspend fun checkAlreadyInBookShelf(book: Book): RespondCheckInBookShelf? {
		if (!isNetworkConnected()) throw NetworkIsNotConnectedException()

		val response = bookChatApi.checkAlreadyInBookShelf(book.isbn, book.publishAt)
		when (response.code()) {
			200, 404 -> {
				return response.body()
			}

			else -> throw Exception(
				createExceptionMessage(
					response.code(),
					response.errorBody()?.string()
				)
			)
		}
	}

	override suspend fun getBookShelfBooks(
		size: String,
		page: String,
		readingStatus: ReadingStatus,
		sort: SearchSortOption
	): ResponseGetBookShelfBooks {
		return bookChatApi.getBookShelfBooks(
			size = size,
			page = page,
			readingStatus = readingStatus,
			sort = sort
		)
	}

	private fun isNetworkConnected(): Boolean {
		return App.instance.isNetworkConnected()
	}

	private fun createExceptionMessage(responseCode: Int, responseErrorBody: String?): String {
		return "responseCode : $responseCode , responseErrorBody : $responseErrorBody"
	}

	companion object {
		private val SEARCH_BOOKS_ITEM_LOAD_SIZE = BookImgSizeManager.flexBoxBookSpanSize * 2
	}
}