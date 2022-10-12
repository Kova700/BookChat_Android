package com.example.bookchat.api

import com.example.bookchat.data.BookSearchResultDto
import com.example.bookchat.data.User
import com.example.bookchat.utils.OAuth2Provider
import com.example.bookchat.utils.ReadingTaste
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface ApiInterface {

    //이름 중복체크
    @GET("/v1/api/users/profile/nickname")
    fun nicknameDuplicateCheck() :Call<Any>

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

    //유저정보 가져오기 (DTO 수정 필요)
    @GET("/v1/api/users/profile")
    fun getUserProfile() : Call<User>

    //제목으로 도서 검색 => 통합 쿼리 검색 api로 수정해야함
    @GET("/v1/api/books")
    suspend fun getBookFromTitle(
        @Query("title") title:String,
        @Query("size") size:String,
        @Query("page") page:String,
        @Query("sort") sort:String,
    ): Response<BookSearchResultDto>

}