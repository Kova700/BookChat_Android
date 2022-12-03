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
import com.example.bookchat.utils.ReadingStatus
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import okhttp3.MediaType
import okhttp3.RequestBody

class BookRepository {

    /** 책 검색*/
    suspend fun simpleSearchBooks(keyword :String) : BookSearchResult {
        if (!isNetworkConnected()) throw NetworkIsNotConnectedException()

        val response = App.instance.bookApiInterface.getBookFromTitle(
            query = keyword,
            size = SIMPLE_SEARCH_BOOKS_ITEM_LOAD_SIZE.toString(),
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

        return Pager(
            config = PagingConfig( pageSize = SIMPLE_SEARCH_BOOKS_ITEM_LOAD_SIZE, enablePlaceholders = false ),
            pagingSourceFactory = { SearchResultBookDetailPagingSource(keyword) }
        ).flow
    }

    /** 책 서재 등록*/
    suspend fun registerWishBook(requestRegisterWishBook :RequestRegisterWishBook){
        if (!isNetworkConnected()) throw NetworkIsNotConnectedException()

        val response = App.instance.bookApiInterface.registerWishBook(requestRegisterWishBook)
        when(response.code()){
            200 -> { }
            else -> throw Exception(createExceptionMessage(response.code(),response.errorBody()?.string()))
        }
    }

    suspend fun registerReadingBook(requestRegisterReadingBook: RequestRegisterReadingBook){
        if (!isNetworkConnected()) throw NetworkIsNotConnectedException()

        val response = App.instance.bookApiInterface.registerReadingBook(requestRegisterReadingBook)
        when(response.code()){
            200 -> { }
            else -> throw Exception(createExceptionMessage(response.code(),response.errorBody()?.string()))
        }
    }

    suspend fun registerCompleteBook(requestRegisterCompleteBook: RequestRegisterCompleteBook){
        if (!isNetworkConnected()) throw NetworkIsNotConnectedException()

        val response = App.instance.bookApiInterface.registerCompleteBook(requestRegisterCompleteBook)
        when(response.code()){
            200 -> { }
            else -> throw Exception(createExceptionMessage(response.code(),response.errorBody()?.string()))
        }
    }

    /**서재 도서 삭제*/
    suspend fun deleteBookShelfBook(bookId :Long){
        Log.d(TAG, "BookRepository: deleteBookShelfBook() - called")
        if (!isNetworkConnected()) throw NetworkIsNotConnectedException()

        val response = App.instance.bookApiInterface.deleteBookShelfBook(bookId)
        when(response.code()){
            200 -> { }
            else -> throw Exception(createExceptionMessage(response.code(),response.errorBody()?.string()))
        }
    }

    /**서재 도서 상태변경*/
    suspend fun changeBookShelfBookStatus(book :BookShelfItem, readingStatus : ReadingStatus){
        Log.d(TAG, "BookRepository: changeBookShelfBookStatus() - called")
        if (!isNetworkConnected()) throw NetworkIsNotConnectedException()

        val requestBody = getRequestBody(
            content = hashMapOf( Pair(JSON_KEY_READINGSTATUS,readingStatus) ),
            contentType = UserRepository.CONTENT_TYPE_JSON
        )

        val response = App.instance.bookApiInterface.changeBookShelfBookStatus(book.bookId,requestBody)
        when(response.code()){
            200 -> { }
            else -> throw Exception(createExceptionMessage(response.code(),response.errorBody()?.string()))
        }
    }

    private fun <T> getRequestBody(
        content : T,
        contentType : String
    ) : RequestBody {
        val jsonString = Gson().toJson(content)
        val requestBody = RequestBody.create(MediaType.parse(contentType),jsonString)
        return requestBody
    }


    private fun isNetworkConnected() :Boolean{
        return App.instance.isNetworkConnected()
    }

    private fun createExceptionMessage(responseCode :Int, responseErrorBody :String?) :String {
        return "responseCode : $responseCode , responseErrorBody : $responseErrorBody"
    }

    companion object{
        private const val SIMPLE_SEARCH_BOOKS_ITEM_LOAD_SIZE = 6
        const val WISH_TAP_BOOKS_ITEM_LOAD_SIZE = 6
        const val READING_TAP_BOOKS_ITEM_LOAD_SIZE = 4
        const val JSON_KEY_READINGSTATUS = "readingStatus"
    }
}