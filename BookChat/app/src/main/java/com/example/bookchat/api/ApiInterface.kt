package com.example.bookchat.api

import com.example.bookchat.data.BookSearchResultDto
import com.example.bookchat.data.User
import com.example.bookchat.utils.ReadingTaste
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface ApiInterface {

    //이름 중복체크
    @GET("/v1/api/users/profile/nickname")
    fun nicknameDuplicateCheck() :Call<Any>

    //회원가입
    @Multipart
    @JvmSuppressWildcards //자동 형변환 방지
    @POST("/v1/api/users")
    fun signUp(
        @Part("nickname") nickname: String,
        @Part("userEmail") userEmail: String,
        @Part("oauth2Provider") oauth2Provider:String,
        @Part("defaultProfileImageType") defaultProfileImageType: Int,
        @Part("readingTastes") readingTastes: List<ReadingTaste>,
        @Part userProfileImage: MultipartBody.Part
        ) :Call<Any>

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