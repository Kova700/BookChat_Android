package com.example.bookchat.api

import com.example.bookchat.data.*
import com.example.bookchat.request.RequestChangeBookStatus
import com.example.bookchat.request.RequestRegisterBookReport
import com.example.bookchat.request.RequestRegisterBookShelfBook
import com.example.bookchat.utils.BookSearchSortOption
import com.example.bookchat.utils.ReadingStatus
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface BookChatApiInterface {
    //API 테스트
    //@POST("/3862dd02-166b-4975-9a3c-1666ebd4fcfa")

    /**------------유저------------*/

    @GET("/v1/api/users/profile/nickname")
    suspend fun requestNameDuplicateCheck(
        @Query("nickname") nickname :String
    ) : Response<Unit>

    @Multipart
    @POST("/v1/api/users/signup")
    suspend fun signUp(
        @Header("OIDC") idToken :String,
        @Part userProfileImage: MultipartBody.Part? = null,
        @Part("userSignUpRequest") userSignUpRequest: RequestBody
    ) : Response<Unit>

    @POST("/v1/api/users/signin")
    suspend fun signIn(
        @Header("OIDC") idToken :String,
        @Body oauth2Provider : RequestBody
    ) : Response<Token>

    @DELETE("/v1/api/users")
    suspend fun withdraw(
    ) : Response<Unit>

    @GET("/v1/api/users/profile")
    suspend fun getUserProfile(
    ) : Response<User>

    /**------------도서------------*/

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

    @PUT("/v1/api/bookshelf/books/{bookId}")
    suspend fun registerReadingPage(
        @Path("bookId") bookId: Long
    ): Response<Unit>

    /**------------독후감------------*/

    @GET("/v1/api/books/{bookId}/report")
    suspend fun getBookReport(
        @Path("bookId") bookId: Long,
    ): Response<BookReport>

    @POST("/v1/api/books/{bookId}/report")
    suspend fun registerBookReport(
        @Path("bookId") bookId: Long,
        @Body requestRegisterBookReport : RequestRegisterBookReport
    ): Response<Unit>

    @DELETE("/v1/api/books/{bookId}/report")
    suspend fun deleteBookReport(
        @Path("bookId") bookId: Long,
    ): Response<Unit>

    @PUT("/v1/api/books/{bookId}/report")
    suspend fun reviseBookReport(
        @Path("bookId") bookId: Long,
        @Body requestRegisterBookReport : RequestRegisterBookReport
    ): Response<Unit>

}