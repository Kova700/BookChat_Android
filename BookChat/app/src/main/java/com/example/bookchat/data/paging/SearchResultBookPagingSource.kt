package com.example.bookchat.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.bookchat.App
import com.example.bookchat.data.response.NetworkIsNotConnectedException
import com.example.bookchat.data.response.SearchingMeta
import com.example.bookchat.domain.model.Book
import com.example.bookchat.domain.repository.BookSearchRepository

class SearchResultBookPagingSource(
	private val searchKeyword: String,
	private val bookSearchRepository: BookSearchRepository
) : PagingSource<Int, Book>() {
	private lateinit var response: List<Book>

	override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Book> {
		if (!isNetworkConnected()) return LoadResult.Error(NetworkIsNotConnectedException())
		val page = params.key ?: STARTING_PAGE_INDEX

		try {
			response = bookSearchRepository.searchBooks(
				keyword = searchKeyword,
				loadSize = params.loadSize,
				page = page,
			)

//			return getLoadResult(
//				data = response,
//				searchingMeta = response.searchingMeta,
//				nowPage = page
//			)
			return LoadResult.Page(
				data = response,
				prevKey = 0,
				nextKey = 0
			)

		} catch (e: Exception) {
			return LoadResult.Error(e)
		}
	}

	override fun getRefreshKey(state: PagingState<Int, Book>): Int? {
//		return state.anchorPosition?.let { anchorPosition ->
//			state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
//				?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
//		}
		return null
	}

//	private fun getLoadResult(
//		data: List<NetWorkBook>,
//		nowPage: Int,
//		searchingMeta: SearchingMeta
//	): LoadResult<Int, NetWorkBook> {
//		return try {
//			LoadResult.Page(
//				data = data,
//				prevKey = if (nowPage == 1) null else nowPage - 1,
//				nextKey = getNextKey(nowPage, searchingMeta)
//			)
//		} catch (exception: Exception) {
//			LoadResult.Error(exception)
//		}
//	}

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