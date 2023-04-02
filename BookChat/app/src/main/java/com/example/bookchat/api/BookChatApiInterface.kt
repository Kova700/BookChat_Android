package com.example.bookchat.api

import com.example.bookchat.data.*
import com.example.bookchat.data.request.*
import com.example.bookchat.data.response.*
import com.example.bookchat.utils.BookSearchSortOption
import com.example.bookchat.utils.BookSearchSortOption.ACCURACY
import com.example.bookchat.utils.ReadingStatus
import com.example.bookchat.utils.SearchSortOption
import com.example.bookchat.utils.SearchSortOption.UPDATED_AT_DESC
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
        @Query("sort") sort: BookSearchSortOption = ACCURACY
    ): Response<ResponseGetBookSearch>

    @POST("/v1/api/bookshelves")
    suspend fun registerBookShelfBook(
        @Body requestRegisterBookShelfBook: RequestRegisterBookShelfBook
    ): Response<Unit>

    @GET("/v1/api/bookshelves")
    suspend fun getBookShelfBooks(
        @Query("size") size: String,
        @Query("page") page: String,
        @Query("readingStatus") readingStatus: ReadingStatus,
        @Query("sort") sort: SearchSortOption = UPDATED_AT_DESC
    ): Response<ResponseGetBookShelfBooks>

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

    @GET("/v1/api/bookshelves/{bookShelfId}/report")
    suspend fun getBookReport(
        @Path("bookShelfId") bookShelfId: Long,
    ): Response<BookReport>

    @POST("/v1/api/bookshelves/{bookShelfId}/report")
    suspend fun registerBookReport(
        @Path("bookShelfId") bookShelfId: Long,
        @Body requestRegisterBookReport: RequestRegisterBookReport
    ): Response<Unit>

    @DELETE("/v1/api/bookshelves/{bookShelfId}/report")
    suspend fun deleteBookReport(
        @Path("bookShelfId") bookShelfId: Long,
    ): Response<Unit>

    @PUT("/v1/api/bookshelves/{bookShelfId}/report")
    suspend fun reviseBookReport(
        @Path("bookShelfId") bookShelfId: Long,
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
        @Query("postCursorId") postCursorId: Int?,
    ): Response<ResponseGetAgony>

    @DELETE("/v1/api/bookshelves/{bookShelfId}/agonies/{bookIdListString}")
    suspend fun deleteAgony(
        @Path("bookShelfId") bookShelfId: Long,
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
        @Query("postCursorId") postCursorId: Int?,
        @Query("size") size: String,
        @Query("sort") sort: SearchSortOption
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

    /**------------채팅방 목록------------*/

    @GET("/v1/api/users/chatrooms")
    suspend fun getUserChatRoomList(
        @Query("postCursorId") postCursorId: Int,
        @Query("size") size: String,
    ): Response<ResponseGetUserChatRoomList>

    @Multipart
    @POST("/v1/api/chatrooms")
    suspend fun makeChatRoom(
        @Part("createChatRoomRequest") requestMakeChatRoom: RequestMakeChatRoom,
        @Part chatRoomImage: MultipartBody.Part? = null
    ): Response<Unit>

    @GET("/v1/api/chatrooms")
    suspend fun searchChatRoom(
        @Query("postCursorId") postCursorId: Int,
        @Query("size") size: String,
        @Query("roomName") roomName: String? = null,
        @Query("title") title: String? = null,
        @Query("isbn") isbn: String? = null,
        @Query("tags") tags: String? = null,
    ): Response<ResponseGetSearchChatRoomList>

}