package com.kova700.bookchat.core.network.bookchat.bookreport

import com.kova700.bookchat.core.data.util.model.network.BookChatApiResult
import com.kova700.bookchat.core.network.bookchat.bookreport.model.request.RequestRegisterBookReport
import com.kova700.bookchat.core.network.bookchat.bookreport.model.response.BookReportResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface BookReportApi {

	@GET("/v1/api/bookshelves/{bookShelfId}/report")
	suspend fun getBookReport(
		@Path("bookShelfId") bookShelfId: Long,
	): BookChatApiResult<BookReportResponse>

	@POST("/v1/api/bookshelves/{bookShelfId}/report")
	suspend fun registerBookReport(
		@Path("bookShelfId") bookShelfId: Long,
		@Body requestRegisterBookReport: RequestRegisterBookReport,
	)

	@DELETE("/v1/api/bookshelves/{bookShelfId}/report")
	suspend fun deleteBookReport(
		@Path("bookShelfId") bookShelfId: Long,
	)

	@PUT("/v1/api/bookshelves/{bookShelfId}/report")
	suspend fun reviseBookReport(
		@Path("bookShelfId") bookShelfId: Long,
		@Body requestRegisterBookReport: RequestRegisterBookReport,
	)
}