package com.kova700.bookchat.core.data.search.book.internal

import com.kova700.bookchat.core.data.search.book.external.BookSearchRepository
import com.kova700.bookchat.core.data.search.book.external.model.Book
import com.kova700.bookchat.core.data.search.book.external.model.BookSearchSortOption
import com.kova700.bookchat.core.network.bookchat.search.SearchApi
import com.kova700.bookchat.core.network.bookchat.search.model.book.mapper.toBook
import com.kova700.bookchat.core.network.bookchat.search.model.book.mapper.toNetWork
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import javax.inject.Inject

class BookSearchRepositoryImpl @Inject constructor(
	private val searchApi: SearchApi,
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

		val response = searchApi.getBookSearchResult(
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