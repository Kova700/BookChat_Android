package com.example.bookchat.data.repository

import com.example.bookchat.App
import com.example.bookchat.data.network.BookChatApi
import com.example.bookchat.data.mapper.toBook
import com.example.bookchat.data.response.NetworkIsNotConnectedException
import com.example.bookchat.domain.model.Book
import com.example.bookchat.domain.repository.BookSearchRepository
import javax.inject.Inject

class BookSearchRepositoryImpl @Inject constructor(
	private val bookChatApi: BookChatApi
) : BookSearchRepository {

	override suspend fun searchBooks(
		keyword: String,
		loadSize: Int,
		page: Int
	): List<Book> {
		if (!isNetworkConnected()) throw NetworkIsNotConnectedException()

		val response = bookChatApi.getBookSearchResult(
			query = keyword,
			size = loadSize.toString(),
			page = page.toString()
		)

		return response.bookResponses.toBook()
	}

	private fun isNetworkConnected(): Boolean {
		return App.instance.isNetworkConnected()
	}
}