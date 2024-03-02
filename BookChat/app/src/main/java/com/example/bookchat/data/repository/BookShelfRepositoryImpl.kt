package com.example.bookchat.data.repository

import com.example.bookchat.App
import com.example.bookchat.data.Book
import com.example.bookchat.data.BookShelfItem
import com.example.bookchat.data.api.BookChatApi
import com.example.bookchat.data.request.RequestChangeBookStatus
import com.example.bookchat.data.request.RequestRegisterBookShelfBook
import com.example.bookchat.data.response.NetworkIsNotConnectedException
import com.example.bookchat.data.response.RespondCheckInBookShelf
import com.example.bookchat.data.response.ResponseGetBookShelfBooks
import com.example.bookchat.domain.repository.BookShelfRepository
import com.example.bookchat.utils.ReadingStatus
import com.example.bookchat.utils.SearchSortOption
import javax.inject.Inject

class BookShelfRepositoryImpl @Inject constructor(
	private val bookChatApi: BookChatApi
) : BookShelfRepository {

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

	override suspend fun registerBookShelfBook(requestRegisterBookShelfBook: RequestRegisterBookShelfBook) {
		if (!isNetworkConnected()) throw NetworkIsNotConnectedException()
		bookChatApi.registerBookShelfBook(requestRegisterBookShelfBook)
	}

	override suspend fun deleteBookShelfBook(bookId: Long) {
		if (!isNetworkConnected()) throw NetworkIsNotConnectedException()
		bookChatApi.deleteBookShelfBook(bookId)
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
		bookChatApi.changeBookShelfBookStatus(book.bookShelfId, requestBody)
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

	private fun createExceptionMessage(responseCode: Int, responseErrorBody: String?): String {
		return "responseCode : $responseCode , responseErrorBody : $responseErrorBody"
	}

	private fun isNetworkConnected(): Boolean {
		return App.instance.isNetworkConnected()
	}
}