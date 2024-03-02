package com.example.bookchat.data.repository

import com.example.bookchat.App
import com.example.bookchat.data.api.BookChatApi
import com.example.bookchat.data.response.NetworkIsNotConnectedException
import com.example.bookchat.data.response.ResponseGetBookSearch
import com.example.bookchat.domain.repository.BookSearchRepository
import javax.inject.Inject

class BookSearchRepositoryImpl @Inject constructor(
	private val bookChatApi: BookChatApi
) : BookSearchRepository {

	override suspend fun searchBooks(
		keyword: String,
		loadSize: Int,
		page: Int
	): ResponseGetBookSearch {
		if (!isNetworkConnected()) throw NetworkIsNotConnectedException()

		return bookChatApi.getBookSearchResult(
			query = keyword,
			size = loadSize.toString(),
			page = page.toString()
		)
	}

	private fun isNetworkConnected(): Boolean {
		return App.instance.isNetworkConnected()
	}
}