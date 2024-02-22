package com.example.bookchat.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.bookchat.App
import com.example.bookchat.data.BookShelfItem
import com.example.bookchat.data.response.BookShelfMeta
import com.example.bookchat.data.response.NetworkIsNotConnectedException
import com.example.bookchat.data.response.ResponseBodyEmptyException
import com.example.bookchat.data.response.ResponseGetBookShelfBooks
import com.example.bookchat.domain.repository.BookRepository
import com.example.bookchat.utils.ReadingStatus
import retrofit2.Response

class ReadingBookTapPagingSource(
	private val bookRepository: BookRepository
) : PagingSource<Int, Pair<BookShelfItem, Long>>() {
	private lateinit var response: ResponseGetBookShelfBooks

	override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Pair<BookShelfItem, Long>> {
		if (!isNetworkConnected()) return LoadResult.Error(NetworkIsNotConnectedException())
		val page = params.key ?: STARTING_PAGE_INDEX

		return try {
			response = bookRepository.getBookShelfBooks(
				size = params.loadSize.toString(),
				page = page.toString(),
				readingStatus = ReadingStatus.READING
			)
			getLoadResult(response, page)
		} catch (e: Exception) {
			LoadResult.Error(e)
		}
		//PagingSource에서 호출해서 서버로부터 데이터 가져오고,
		//Room에 저장
		//Rcv에서는 Room으로부터 PagingSource받아서 가져오는 로직,
		//이게 Remotemadiator
		//문제점 : 이미 가져와있는 데이터가 있으면, 해당 데이터가 맞다고 판단하고
		//  더이상 데이터를 로드하지 않음
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