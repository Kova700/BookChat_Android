package com.example.bookchat.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.bookchat.App
import com.example.bookchat.data.BookShelfItem
import com.example.bookchat.data.response.BookShelfMeta
import com.example.bookchat.data.response.NetworkIsNotConnectedException
import com.example.bookchat.data.response.ResponseGetBookShelfBooks
import com.example.bookchat.domain.repository.BookShelfRepository
import com.example.bookchat.utils.ReadingStatus

class CompleteBookTapPagingSource(
	private val bookShelfRepository: BookShelfRepository
) : PagingSource<Int, Pair<BookShelfItem, Long>>() {
	private lateinit var response: ResponseGetBookShelfBooks

	override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Pair<BookShelfItem, Long>> {
		if (!isNetworkConnected()) return LoadResult.Error(NetworkIsNotConnectedException())
		val page = params.key ?: STARTING_PAGE_INDEX

		return try {
			response = bookShelfRepository.getBookShelfBooks(
				size = params.loadSize.toString(),
				page = page.toString(),
				readingStatus = ReadingStatus.COMPLETE
			)
			getLoadResult(response, page)
		} catch (e: Exception) {
			LoadResult.Error(e)
		}
	}

	override fun getRefreshKey(state: PagingState<Int, Pair<BookShelfItem, Long>>): Int? {
		return 0
	}

	private fun getLoadResult(
		response: ResponseGetBookShelfBooks,
		nowPage: Int
	): LoadResult<Int, Pair<BookShelfItem, Long>> {
		return try {
			LoadResult.Page(
				data = response.contents.map { Pair(it, response.pageMeta.totalElements) },
				prevKey = if (nowPage == STARTING_PAGE_INDEX) null else nowPage - 1,
				nextKey = getNextKey(nowPage, response.pageMeta)
			)
		} catch (exception: Exception) {
			LoadResult.Error(exception)
		}
	}

	private fun getNextKey(
		nowPage: Int,
		bookShelfMeta: BookShelfMeta
	): Int? {
		if (bookShelfMeta.last) return null

		if (nowPage == STARTING_PAGE_INDEX) {
			return nowPage + 3
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
		private const val STARTING_PAGE_INDEX = 0
	}
}