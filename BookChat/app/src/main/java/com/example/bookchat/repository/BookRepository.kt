package com.example.bookchat.repository

import android.util.Log
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.bookchat.App
import com.example.bookchat.data.Book
import com.example.bookchat.data.BookSearchResult
import com.example.bookchat.paging.SearchResultBookDetailPagingSource
import com.example.bookchat.response.NetworkIsNotConnectedException
import com.example.bookchat.response.ResponseBodyEmptyException
import com.example.bookchat.utils.Constants.TAG
import kotlinx.coroutines.flow.Flow

class BookRepository {

    suspend fun simpleSearchBooks(keyword :String) : BookSearchResult {
        if (!isNetworkConnected()) throw NetworkIsNotConnectedException()

        val response = App.instance.bookApiInterface.getBookFromTitle(
            query = keyword,
            size = SIMPLE_SEARCH_BOOKS_ITEM_LOAD_SIZE,
            page = 1.toString()
        )

        when(response.code()){
            200 -> {
                val bookSearchResultDto = response.body()
                bookSearchResultDto?.let { return bookSearchResultDto }
                throw ResponseBodyEmptyException(response.errorBody()?.string())
            }
            else -> throw Exception(createExceptionMessage(response.code(),response.errorBody()?.string()))
        }

    }

    fun detailSearchBooks(keyword :String) : Flow<PagingData<Book>> {
        Log.d(TAG, "BookRepository: detailSearchBooks() - called")

        return Pager(
            config = PagingConfig( pageSize = 5, enablePlaceholders = false ),
            pagingSourceFactory = { SearchResultBookDetailPagingSource(keyword) }
        ).flow
    }

    private fun isNetworkConnected() :Boolean{
        return App.instance.isNetworkConnected()
    }

    private fun createExceptionMessage(responseCode :Int, responseErrorBody :String?) :String {
        return "responseCode : $responseCode , responseErrorBody : $responseErrorBody"
    }

    companion object{
        const val SIMPLE_SEARCH_BOOKS_ITEM_LOAD_SIZE = 6.toString()
    }
}