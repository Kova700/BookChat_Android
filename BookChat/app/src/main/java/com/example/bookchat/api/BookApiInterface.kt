package com.example.bookchat.api

import com.example.bookchat.data.*
import com.example.bookchat.utils.BookSearchSortOption
import com.example.bookchat.utils.ReadingStatus
import retrofit2.Response
import retrofit2.http.*

interface BookApiInterface {
    //API 테스트
    //@POST("/3862dd02-166b-4975-9a3c-1666ebd4fcfa")

    @GET("/v1/api/books")
    suspend fun getBookSearchResult(
        @Query("query") query: String,
        @Query("size") size: String,
        @Query("page") page: String,
        @Query("sort") sort: BookSearchSortOption = BookSearchSortOption.ACCURACY,
    ): Response<BookSearchResult>

    @POST("/v1/api/bookshelf/books")
    suspend fun registerBookShelfBook(
        @Body requestRegisterBookShelfBook: RequestRegisterBookShelfBook
    ): Response<Unit>

    @GET("/v1/api/bookshelf/books")
    suspend fun getBookShelfBooks(
        @Query("size") size: String,
        @Query("page") page: String,
        @Query("readingStatus") readingStatus: ReadingStatus,
        @Query("sort") sort: String = "id,DESC"
    ): Response<BookShelfResult>

    @DELETE("/v1/api/bookshelf/books/{bookId}")
    suspend fun deleteBookShelfBook(
        @Path("bookId") bookId: Long
    ): Response<Unit>

    @PUT("/v1/api/bookshelf/books/{bookId}")
    suspend fun changeBookShelfBookStatus(
        @Path("bookId") bookId: Long,
        @Body requestChangeBookStatus: RequestChangeBookStatus
    ): Response<Unit>

    @GET("/v1/api/bookshelf/books/existence")
    suspend fun checkAlreadyInBookShelf(
        @Query("isbn") isbn :String,
        @Query("publishAt") publishAt :String,
    ): Response<RespondCheckInBookShelf>
}