package com.example.bookchat.api

import com.example.bookchat.data.BookReport
import com.example.bookchat.data.BookReportRequest
import retrofit2.Response
import retrofit2.http.*

interface BookReportApiInterface {

    @GET("/v1/api/books/{bookId}/report")
    suspend fun getBookReport(
        @Path("bookId") bookId: Long,
    ): Response<BookReport>

    @POST("/v1/api/books/{bookId}/report")
    suspend fun registerBookReport(
        @Path("bookId") bookId: Long,
        @Body bookReportRequest : BookReportRequest
    ): Response<Unit>

    @DELETE("/v1/api/books/{bookId}/report")
    suspend fun deleteBookReport(
        @Path("bookId") bookId: Long,
    ): Response<Unit>

    @PUT("/v1/api/books/{bookId}/report")
    suspend fun reviseBookReport(
        @Path("bookId") bookId: Long,
        @Body bookReportRequest : BookReportRequest
    ): Response<Unit>

}