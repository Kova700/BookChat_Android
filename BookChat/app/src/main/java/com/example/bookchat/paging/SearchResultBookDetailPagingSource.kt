package com.example.bookchat.paging

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.bookchat.App
import com.example.bookchat.data.Book
import com.example.bookchat.data.SearchingMeta
import com.example.bookchat.response.NetworkIsNotConnectedException
import com.example.bookchat.response.ResponseBodyEmptyException
import com.example.bookchat.utils.Constants.TAG

class SearchResultBookDetailPagingSource(
    private val keyword :String
) :PagingSource<Int, Book>(){

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Book> {
        if(!isNetworkConnected()) return LoadResult.Error(NetworkIsNotConnectedException())

        val page = params.key ?: STARTING_PAGE_INDEX

        val response = App.instance.bookApiInterface.getBookFromTitle(
            query = keyword,
            size = params.loadSize.toString(), //처음엔 x3되어서 요첨됨
            page = page.toString(),
        )

        when(response.code()){
            200 -> {
                val bookSearchResult = response.body()
                bookSearchResult?.let {
                    val pagedBookList = bookSearchResult.bookResponses
                    Log.d(TAG, "BookSearchResultPagingSource: load() - pagedBookList : $pagedBookList")
                    val meta = bookSearchResult.searchingMeta
                    return getLoadResult(pagedBookList,page,meta)
                }
                return LoadResult.Error(ResponseBodyEmptyException(response.errorBody()?.string()))
            }
            else -> return LoadResult.Error(Exception(createExceptionMessage(response.code(),response.errorBody()?.string())))
        }

    }

    override fun getRefreshKey(state: PagingState<Int, Book>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    private fun getLoadResult(
        data :List<Book>,
        nowPage :Int,
        searchingMeta :SearchingMeta
    ): LoadResult<Int, Book>{
        return try {
            LoadResult.Page(
                data = data,
                prevKey = if (nowPage == 1) null else nowPage - 1,
                nextKey = getNextKey(nowPage,searchingMeta)
            )
        }catch (exception :Exception){
            LoadResult.Error(exception)
        }
    }

    private fun getNextKey(
        nowPage :Int,
        searchingMeta :SearchingMeta
    ) :Int?{
        if(searchingMeta.isEnd) return null

        if (nowPage == STARTING_PAGE_INDEX){
            return 4 * STARTING_PAGE_INDEX
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
        private const val STARTING_PAGE_INDEX = 1
    }
}