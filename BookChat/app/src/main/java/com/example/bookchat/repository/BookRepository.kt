package com.example.bookchat.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.bookchat.App
import com.example.bookchat.data.Book
import com.example.bookchat.data.BookShelfItem
import com.example.bookchat.data.request.RequestChangeBookStatus
import com.example.bookchat.data.request.RequestRegisterBookShelfBook
import com.example.bookchat.data.response.NetworkIsNotConnectedException
import com.example.bookchat.data.response.RespondCheckInBookShelf
import com.example.bookchat.data.response.ResponseBodyEmptyException
import com.example.bookchat.data.response.ResponseGetBookSearch
import com.example.bookchat.paging.SearchResultBookDetailPagingSource
import com.example.bookchat.utils.BookImgSizeManager
import com.example.bookchat.utils.ReadingStatus
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class BookRepository @Inject constructor(){

    suspend fun simpleSearchBooks(keyword :String) : ResponseGetBookSearch {
        if (!isNetworkConnected()) throw NetworkIsNotConnectedException()
        val loadSize = SEARCH_BOOKS_ITEM_LOAD_SIZE * 2

        val response = App.instance.bookChatApiClient.getBookSearchResult(
            query = keyword,
            size = loadSize.toString(),
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
            config = PagingConfig( pageSize = SEARCH_BOOKS_ITEM_LOAD_SIZE, enablePlaceholders = false ),
            pagingSourceFactory = { SearchResultBookDetailPagingSource(keyword) }
        ).flow
    }

    suspend fun registerBookShelfBook(requestRegisterBookShelfBook: RequestRegisterBookShelfBook){
        if (!isNetworkConnected()) throw NetworkIsNotConnectedException()
        val response = App.instance.bookChatApiClient.registerBookShelfBook(requestRegisterBookShelfBook)
        when(response.code()){
            200 -> { }
            else -> throw Exception(createExceptionMessage(response.code(),response.errorBody()?.string()))
        }
    }

    suspend fun deleteBookShelfBook(bookId :Long){
        if (!isNetworkConnected()) throw NetworkIsNotConnectedException()

        val response = App.instance.bookChatApiClient.deleteBookShelfBook(bookId)
        when(response.code()){
            200 -> { }
            else -> throw Exception(createExceptionMessage(response.code(),response.errorBody()?.string()))
        }
    }

    suspend fun changeBookShelfBookStatus(book :BookShelfItem, readingStatus : ReadingStatus){
        if (!isNetworkConnected()) throw NetworkIsNotConnectedException()

        val requestBody = RequestChangeBookStatus(
            readingStatus = readingStatus,
            star = book.star,
            pages = book.pages
        )
        val response = App.instance.bookChatApiClient.changeBookShelfBookStatus(book.bookShelfId, requestBody)
        when(response.code()){
            200 -> { }
            else -> throw Exception(createExceptionMessage(response.code(),response.errorBody()?.string()))
        }
    }

    suspend fun checkAlreadyInBookShelf(book :Book) : RespondCheckInBookShelf? {
        if (!isNetworkConnected()) throw NetworkIsNotConnectedException()

        val response = App.instance.bookChatApiClient.checkAlreadyInBookShelf(book.isbn, book.publishAt)
        when(response.code()){
            200, 404 -> {
                val respondCheckInBookShelf = response.body()
                return respondCheckInBookShelf
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
        private val SEARCH_BOOKS_ITEM_LOAD_SIZE = BookImgSizeManager.flexBoxBookSpanSize * 2
    }
}