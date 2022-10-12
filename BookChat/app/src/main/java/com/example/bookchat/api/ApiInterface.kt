package com.example.bookchat.api

import com.example.bookchat.data.BookSearchResultDto
import com.example.bookchat.data.Token
import com.example.bookchat.data.User
import com.example.bookchat.utils.OAuth2Provider
import com.example.bookchat.utils.ReadingTaste
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface ApiInterface {

    @GET("/v1/api/users/profile/nickname")
    suspend fun requestNameDuplicateCheck(
        @Query("nickname") nickname :String
    ) :Response<Unit>

    @POST("/v1/api/auth/token")
    suspend fun requestTokenRenewal(
        @Body refreshToken :String
    ): Response<Token>

    //헤더명 수정해야함
    //회원가입
    //이미지(Part) , 부가정보(Body) ==> 현재 이미지 전송오류 있음 (서버 API 수정시 추후 수정)
    @Multipart
    @POST("/v1/api/users/signup")
    suspend fun signUp(
        @Header("Authorization") idToken :String,
        @Header("provider_type") idTokenProvider : OAuth2Provider,
        @Part userProfileImage: MultipartBody.Part? = null,
        @Part(value = "nickname") nickname: String,
        @Part("defaultProfileImageType") defaultProfileImageType: Int,
        @Part("readingTastes") readingTastes: List<@JvmSuppressWildcards ReadingTaste>? = null
    ) :Response<Unit>

    @DELETE("/v1/api/users")
    suspend fun withdraw(
        @Header("Authorization") accessToken :String // 북챗 JWT 토큰
    ) : Response<Unit>

    //헤더명 수정해야함
    @POST("/v1/api/users/signin")
    suspend fun signIn(
        @Header("Authorization") idToken :String,
        @Header("provider_type") oAuth2Provider :OAuth2Provider
    ) : Response<Token>

    @GET("/v1/api/users/profile")
    suspend fun getUserProfile(
        @Header("Authorization") token :String, //임시로 인터셉터 안씀
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