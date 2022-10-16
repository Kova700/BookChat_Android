package com.example.bookchat.api

import com.example.bookchat.data.BookSearchResultDto
import com.example.bookchat.data.Token
import com.example.bookchat.data.User
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiInterface {
    //API 테스트
    //@POST("/3862dd02-166b-4975-9a3c-1666ebd4fcfa")

    @GET("/v1/api/users/profile/nickname")
    suspend fun requestNameDuplicateCheck(
        @Query("nickname") nickname :String
    ) :Response<Unit>

    @POST("/v1/api/auth/token")
    suspend fun requestTokenRenewal(
        @Body refreshToken :String
    ): Response<Token>

    //회원가입
    @Multipart
    @POST("/v1/api/users/signup")
    suspend fun signUp(
        @Header("OIDC") idToken :String,
        @Part userProfileImage: MultipartBody.Part? = null,
        @Part("userSignUpRequestDto") userSignUpRequestDto: RequestBody
    ) :Response<Unit>

    @POST("/v1/api/users/signin")
    suspend fun signIn(
        @Header("OIDC") idToken :String,
        @Body oauth2Provider :RequestBody
    ) : Response<Token>

    @DELETE("/v1/api/users")
    suspend fun withdraw(
    ) : Response<Unit>

    @GET("/v1/api/users/profile")
    suspend fun getUserProfile(
    ) : Response<User>

    //제목으로 도서 검색 => 통합 쿼리 검색 api로 수정해야함
    @GET("/v1/api/books")
    suspend fun getBookFromTitle(
        @Query("title") title:String,
        @Query("size") size:String,
        @Query("page") page:String,
        @Query("sort") sort:String,
    ): Response<BookSearchResultDto>

}