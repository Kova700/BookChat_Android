package com.example.bookchat.paging

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.bookchat.App
import com.example.bookchat.data.BookShelfItem
import com.example.bookchat.data.BookShelfMeta
import com.example.bookchat.data.BookShelfResult
import com.example.bookchat.response.NetworkIsNotConnectedException
import com.example.bookchat.response.ResponseBodyEmptyException
import com.example.bookchat.utils.Constants.TAG
import com.example.bookchat.utils.ReadingStatus

class ReadingBookTapPagingSource : PagingSource<Int, Pair<BookShelfItem, Long>>(){

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Pair<BookShelfItem, Long>> {
        if(!isNetworkConnected()) return LoadResult.Error(NetworkIsNotConnectedException())

        val page = params.key ?: STARTING_PAGE_INDEX

        val response = App.instance.bookChatApiClient.getBookShelfBooks(
            size = params.loadSize.toString(),
            page = page.toString(),
            readingStatus = ReadingStatus.READING
        )

        when(response.code()){
            200 -> {
                val bookShelfResult = response.body()
                Log.d(TAG, "ReadingBookTapPagingSource: load() - bookShelfResult : $bookShelfResult")
                bookShelfResult?.let { return getLoadResult(bookShelfResult,page) }
                throw ResponseBodyEmptyException(response.errorBody()?.string())
            }
            else -> throw Exception(createExceptionMessage(response.code(),response.errorBody()?.string()))
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Pair<BookShelfItem, Long>>): Int? {
        return 0
    }

    private fun getLoadResult(
        bookShelfResult : BookShelfResult,
        nowPage :Int
    ): LoadResult<Int, Pair<BookShelfItem, Long>>{
        return try {
            LoadResult.Page(
                data = bookShelfResult.contents.map { Pair(it,bookShelfResult.pageMeta.totalElements) },
                prevKey = if (nowPage == STARTING_PAGE_INDEX) null else nowPage - 1,
                nextKey = getNextKey(nowPage,bookShelfResult.pageMeta)
            )
        }catch (exception :Exception){
            LoadResult.Error(exception)
        }
    }

    private fun getNextKey(
        nowPage :Int,
        bookShelfMeta : BookShelfMeta
    ) :Int?{
        if(bookShelfMeta.last) return null

        if (nowPage == STARTING_PAGE_INDEX){
            return nowPage + 3
        }
        return nowPage + 1
    }

    private fun createExceptionMessage(responseCode :Int, responseErrorBody :String?) :String {
        return "responseCode : $responseCode , responseErrorBody : $responseErrorBody"
    }

    private fun isNetworkConnected() :Boolean{
        return App.instance.isNetworkConnected()
    }

    companion object{
        private const val STARTING_PAGE_INDEX = 0
    }
}