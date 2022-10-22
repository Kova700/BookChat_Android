package com.example.bookchat.api

import com.example.bookchat.data.BookSearchResultDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface BookApiInterface {
    //API 테스트
    //@POST("/3862dd02-166b-4975-9a3c-1666ebd4fcfa")

    //제목으로 도서 검색 => 통합 쿼리 검색 api로 수정해야함
    @GET("/v1/api/books")
    suspend fun getBookFromTitle(
        @Query("title") title:String,
        @Query("size") size:String,
        @Query("page") page:String,
        @Query("sort") sort:String,
    ): Response<BookSearchResultDto>

}