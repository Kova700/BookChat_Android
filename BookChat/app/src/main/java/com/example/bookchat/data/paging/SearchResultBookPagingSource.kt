package com.example.bookchat.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.bookchat.App
import com.example.bookchat.data.Book
import com.example.bookchat.data.response.NetworkIsNotConnectedException
import com.example.bookchat.data.response.ResponseGetBookSearch
import com.example.bookchat.data.response.SearchingMeta
import com.example.bookchat.domain.repository.BookRepository

class SearchResultBookPagingSource(
	private val searchKeyword: String,
	private val bookRepository: BookRepository
) : PagingSource<Int, Book>() {
	private lateinit var response: ResponseGetBookSearch

	override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Book> {
		if (!isNetworkConnected()) return LoadResult.Error(NetworkIsNotConnectedException())
		val page = params.key ?: STARTING_PAGE_INDEX

		try {
			response = bookRepository.searchBooks(
				keyword = searchKeyword,
				loadSize = params.loadSize,
				page = page,
			)

			return getLoadResult(
				data = response.bookResponses,
				searchingMeta = response.searchingMeta,
				nowPage = page
			)

		} catch (e: Exception) {
			return LoadResult.Error(e)
		}
	}

	override fun getRefreshKey(state: PagingState<Int, Book>): Int? {
		return state.anchorPosition?.let { anchorPosition ->
			state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
				?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
		}
	}

	private fun getLoadResult(
		data: List<Book>,
		nowPage: Int,
		searchingMeta: SearchingMeta
	): LoadResult<Int, Book> {
		return try {
			LoadResult.Page(
				data = data,
				prevKey = if (nowPage == 1) null else nowPage - 1,
				nextKey = getNextKey(nowPage, searchingMeta)
			)
		} catch (exception: Exception) {
			LoadResult.Error(exception)
		}
	}

	private fun getNextKey(
		nowPage: Int,
		searchingMeta: SearchingMeta
	): Int? {
		if (searchingMeta.isEnd) return null

		if (nowPage == STARTING_PAGE_INDEX) {
			return 4 * STARTING_PAGE_INDEX
		}
		return nowPage + 1
	}

	private fun createExceptionMessage(responseCode: Int, responseErrorBody: String?): String {
		return "responseCode : $responseCode , responseErrorBody : $responseErrorBody"
	}

	private fun isNetworkConnected(): Boolean {
		return App.instance.isNetworkConnected()
	}

	companion object {
		private const val STARTING_PAGE_INDEX = 1
	}
}