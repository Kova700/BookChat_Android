package com.example.bookchat.api

import com.example.bookchat.data.*
import com.example.bookchat.data.request.*
import com.example.bookchat.request.*
import com.example.bookchat.data.response.RespondCheckInBookShelf
import com.example.bookchat.data.response.ResponseGetAgony
import com.example.bookchat.data.response.ResponseGetAgonyRecord
import com.example.bookchat.utils.BookSearchSortOption
import com.example.bookchat.utils.ReadingStatus
import com.example.bookchat.utils.SearchSortOption
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*
import retrofit2.http.Header

interface BookChatApiInterface {
    //API 테스트
//    @POST("/2bc9634d-f24c-44ed-8215-d6e16121b8ae")

    /**------------유저------------*/

    @GET("/v1/api/users/profile/nickname")
    suspend fun requestNameDuplicateCheck(
        @Query("nickname") nickname: String
    ): Response<Unit>

    @Multipart
    @POST("/v1/api/users/signup")
    suspend fun signUp(
        @Header("OIDC") idToken: String,
        @Part userProfileImage: MultipartBody.Part? = null,
        @Part("userSignUpRequest") requestUserSignUp: RequestUserSignUp
    ): Response<Unit>

    @POST("/v1/api/users/signin")
    suspend fun signIn(
        @Header("OIDC") idToken: String,
        @Body requestUserSignIn: RequestUserSignIn
    ): Response<Token>

    @DELETE("/v1/api/users")
    suspend fun withdraw(
    ): Response<Unit>

    @GET("/v1/api/users/profile")
    suspend fun getUserProfile(
    ): Response<User>

    /**------------도서------------*/

    @GET("/v1/api/books")
    suspend fun getBookSearchResult(
        @Query("query") query: String,
        @Query("size") size: String,
        @Query("page") page: String,
        @Query("sort") sort: BookSearchSortOption = BookSearchSortOption.ACCURACY,
    ): Response<BookSearchResult>

    @POST("/v1/api/bookshelves")
    suspend fun registerBookShelfBook(
        @Body requestRegisterBookShelfBook: RequestRegisterBookShelfBook
    ): Response<Unit>

    @GET("/v1/api/bookshelves")
    suspend fun getBookShelfBooks(
        @Query("size") size: String,
        @Query("page") page: String,
        @Query("readingStatus") readingStatus: ReadingStatus,
        @Query("sort") sort: SearchSortOption = SearchSortOption.DESC //임시
    ): Response<BookShelfResult>

    @DELETE("/v1/api/bookshelves/{bookId}")
    suspend fun deleteBookShelfBook(
        @Path("bookId") bookId: Long
    ): Response<Unit>

    @PUT("/v1/api/bookshelves/{bookId}")
    suspend fun changeBookShelfBookStatus(
        @Path("bookId") bookId: Long,
        @Body requestChangeBookStatus: RequestChangeBookStatus
    ): Response<Unit>

    @GET("/v1/api/bookshelves/book")
    suspend fun checkAlreadyInBookShelf(
        @Query("isbn") isbn: String,
        @Query("publishAt") publishAt: String,
    ): Response<RespondCheckInBookShelf>

    /**------------독후감------------*/

    @GET("/v1/api/books/{bookId}/report")
    suspend fun getBookReport(
        @Path("bookId") bookId: Long,
    ): Response<BookReport>

    @POST("/v1/api/books/{bookId}/report")
    suspend fun registerBookReport(
        @Path("bookId") bookId: Long,
        @Body requestRegisterBookReport: RequestRegisterBookReport
    ): Response<Unit>

    @DELETE("/v1/api/books/{bookId}/report")
    suspend fun deleteBookReport(
        @Path("bookId") bookId: Long,
    ): Response<Unit>

    @PUT("/v1/api/books/{bookId}/report")
    suspend fun reviseBookReport(
        @Path("bookId") bookId: Long,
        @Body requestRegisterBookReport: RequestRegisterBookReport
    ): Response<Unit>

    /**------------고민 ------------*/

    @POST("/v1/api/bookshelves/{bookId}/agonies")
    suspend fun makeAgony(
        @Path("bookId") bookId: Long,
        @Body requestMakeAgony: RequestMakeAgony
    ): Response<Unit>

    @GET("/v1/api/bookshelves/{bookShelfId}/agonies")
    suspend fun getAgony(
        @Path("bookShelfId") bookShelfId: Long,
        @Query("size") size: String,
        @Query("sort") sort: SearchSortOption,
        @Query("postCursorId") postCursorId: Int?, //postCursorId Type 수정해야함
    ): Response<ResponseGetAgony>

    //동시 삭제가 가능하게 선택된 폴더 아이디들을 뒤에 쉼표로 연결해야함
    // ex : /v1/api/agonies/1,2,3
    @DELETE("/v1/api/bookshelves/agonies/{bookIdListString}")
    suspend fun deleteAgony(
        @Path("bookIdListString") bookIdListString: String,
    ): Response<Unit>

    @PUT("/v1/api/bookshelves/{bookShelfId}/agonies/{agonyId}")
    suspend fun reviseAgony(
        @Path("bookShelfId") bookShelfId: Long,
        @Path("agonyId") agonyId: Long,
        @Body requestReviseAgony: RequestReviseAgony
    ): Response<Unit>

    /**------------고민 기록------------*/

    @POST("/v1/api/bookshelves/{bookShelfId}/agonies/{agonyId}/records")
    suspend fun makeAgonyRecord(
        @Path("bookShelfId") bookShelfId: Long,
        @Path("agonyId") agonyId: Long,
        @Body requestMakeAgonyRecord: RequestMakeAgonyRecord
    ): Response<Unit>

    @GET("/v1/api/bookshelves/{bookShelfId}/agonies/{agonyId}/records")
    suspend fun getAgonyRecord(
        @Path("bookShelfId") bookShelfId: Long,
        @Path("agonyId") agonyId: Long,
        @Query("postCursorId") postCursorId: Int?, //postCursorId Type 수정해야함
        @Query("size") size: String,
        @Query("sort") sort: SearchSortOption = SearchSortOption.DESC, //임시
    ): Response<ResponseGetAgonyRecord>

    @DELETE("/v1/api/bookshelves/{bookShelfId}/agonies/{agonyId}/records/{recordId}")
    suspend fun deleteAgonyRecord(
        @Path("bookShelfId") bookShelfId: Long,
        @Path("agonyId") agonyId: Long,
        @Path("recordId") recordId: Long
    ): Response<Unit>

    @PUT("/v1/api/bookshelves/{bookShelfId}/agonies/{agonyId}/records/{recordId}")
    suspend fun reviseAgonyRecord(
        @Path("bookShelfId") bookShelfId: Long,
        @Path("agonyId") agonyId: Long,
        @Path("recordId") recordId: Long,
        @Body requestReviseAgonyRecord: RequestReviseAgonyRecord
    ): Response<Unit>
}