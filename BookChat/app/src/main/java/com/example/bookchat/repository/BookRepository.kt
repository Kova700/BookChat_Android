package com.example.bookchat.repository

import android.util.Log
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.bookchat.App
import com.example.bookchat.data.*
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
                val bookSearchResult = response.body()
                bookSearchResult?.let { return bookSearchResult }
                throw ResponseBodyEmptyException(response.errorBody()?.string())
            }
            else -> throw Exception(createExceptionMessage(response.code(),response.errorBody()?.string()))
        }

    }

    fun detailSearchBooks(keyword :String) : Flow<PagingData<Book>> {
        Log.d(TAG, "BookRepository: detailSearchBooks() - called")

        return Pager(
            config = PagingConfig( pageSize = 6, enablePlaceholders = false ),
            pagingSourceFactory = { SearchResultBookDetailPagingSource(keyword) }
        ).flow
    }

    suspend fun registerWishBook(requestRegisterWishBook :RequestRegisterWishBook){
        if (!isNetworkConnected()) throw NetworkIsNotConnectedException()

        val response = App.instance.bookApiInterface.registerWishBook(requestRegisterWishBook)
        when(response.code()){
            200 -> {

            }
            else -> throw Exception(createExceptionMessage(response.code(),response.errorBody()?.string()))
        }
    }

    suspend fun registerReadingBook(requestRegisterReadingBook: RequestRegisterReadingBook){
        if (!isNetworkConnected()) throw NetworkIsNotConnectedException()

        val response = App.instance.bookApiInterface.registerReadingBook(requestRegisterReadingBook)
        when(response.code()){
            200 -> {

            }
            else -> throw Exception(createExceptionMessage(response.code(),response.errorBody()?.string()))
        }
    }

    suspend fun registerCompleteBook(requestRegisterCompleteBook: RequestRegisterCompleteBook){
        if (!isNetworkConnected()) throw NetworkIsNotConnectedException()

        val response = App.instance.bookApiInterface.registerCompleteBook(requestRegisterCompleteBook)
        when(response.code()){
            200 -> {

            }
            else -> throw Exception(createExceptionMessage(response.code(),response.errorBody()?.string()))
        }
    }

    suspend fun requestGetWishList() :BookShelfResult{
        if (!isNetworkConnected()) throw NetworkIsNotConnectedException()
        val response = App.instance.bookApiInterface.getWishBooks()
        when(response.code()){
            200 -> {
                val bookShelfResult = response.body()
                Log.d(TAG, "BookRepository: requestGetWishList() - bookShelfResult : $bookShelfResult")
                bookShelfResult?.let { return bookShelfResult }
                throw ResponseBodyEmptyException(response.errorBody()?.string())
            }
            else -> throw Exception(createExceptionMessage(response.code(),response.errorBody()?.string()))
        }
    }

    private fun isNetworkConnected() :Boolean{
        return App.instance.isNetworkConnected()
    }

    private fun createExceptionMessage(responseCode :Int, responseErrorBody :String?) :String {
        return "responseCode : $responseCode , responseErrorBody : $responseErrorBody"
    }

    companion object{
        private const val SIMPLE_SEARCH_BOOKS_ITEM_LOAD_SIZE = 6.toString()
    }
}