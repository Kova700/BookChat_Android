package com.example.bookchat.data.repository

import com.example.bookchat.data.mapper.toBook
import com.example.bookchat.data.mapper.toNetWork
import com.example.bookchat.data.network.BookChatApi
import com.example.bookchat.domain.model.Book
import com.example.bookchat.domain.model.BookSearchSortOption
import com.example.bookchat.domain.repository.BookSearchRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import javax.inject.Inject

class BookSearchRepositoryImpl @Inject constructor(
	private val bookChatApi: BookChatApi,
) : BookSearchRepository {
	private val mapBooks = MutableStateFlow<Map<String, Book>>(emptyMap())//(isbn, Book)
	private val books = mapBooks.map { it.values.toList() }.filterNotNull()

	private var cachedSearchKeyword = ""
	private var currentPage: Int = 1
	private var isEndPage = false

	override fun getBooksFLow(initFlag: Boolean): Flow<List<Book>> {
		if (initFlag) clear()
		return books
	}

	override suspend fun search(
		keyword: String,
		loadSize: Int,
		sort: BookSearchSortOption,
	): List<Book> {
		if (cachedSearchKeyword != keyword) {
			clear()
		}
		if (isEndPage) return books.firstOrNull() ?: emptyList()

		val response = bookChatApi.getBookSearchResult(
			query = keyword,
			size = loadSize,
			page = currentPage,
			sort = sort.toNetWork()
		)
		cachedSearchKeyword = keyword
		isEndPage = response.searchingMeta.isEnd
		currentPage += 1

		val newBooks = response.bookSearchResponse.map { it.toBook() }
		mapBooks.update { mapBooks.value + newBooks.associateBy { it.isbn } }
		return books.firstOrNull() ?: emptyList()
	}

	override fun getCachedBook(isbn: String): Book {
		return mapBooks.value[isbn]!!
	}

	override fun clear() {
		mapBooks.update { emptyMap() }
		cachedSearchKeyword = ""
		currentPage = 1
		isEndPage = false
	}
}