package com.kova700.bookchat.core.network.bookchat.agonyrecord

import com.kova700.bookchat.core.data.util.model.network.BookChatApiResult
import com.kova700.bookchat.core.network.bookchat.agonyrecord.model.request.RequestMakeAgonyRecord
import com.kova700.bookchat.core.network.bookchat.agonyrecord.model.request.RequestReviseAgonyRecord
import com.kova700.bookchat.core.network.bookchat.agonyrecord.model.response.AgonyRecordResponse
import com.kova700.bookchat.core.network.bookchat.agonyrecord.model.response.ResponseGetAgonyRecord
import com.kova700.bookchat.core.network.bookchat.common.model.SearchSortOptionNetwork
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface AgonyRecordApi {

	@POST("/v1/api/bookshelves/{bookShelfId}/agonies/{agonyId}/records")
	suspend fun makeAgonyRecord(
		@Path("bookShelfId") bookShelfId: Long,
		@Path("agonyId") agonyId: Long,
		@Body requestMakeAgonyRecord: RequestMakeAgonyRecord,
	): BookChatApiResult<Unit>

	@GET("/v1/api/bookshelves/{bookShelfId}/agonies/{agonyId}/records")
	suspend fun getAgonyRecords(
		@Path("bookShelfId") bookShelfId: Long,
		@Path("agonyId") agonyId: Long,
		@Query("postCursorId") postCursorId: Long?,
		@Query("size") size: Int,
		@Query("sort") sort: SearchSortOptionNetwork,
	): ResponseGetAgonyRecord

	@GET("v1/api/bookshelves/{bookShelfId}/agonies/{agonyId}/records/{recordId}")
	suspend fun getAgonyRecord(
		@Path("bookShelfId") bookShelfId: Long,
		@Path("agonyId") agonyId: Long,
		@Path("recordId") recordId: Long,
	): AgonyRecordResponse

	@DELETE("/v1/api/bookshelves/{bookShelfId}/agonies/{agonyId}/records/{recordId}")
	suspend fun deleteAgonyRecord(
		@Path("bookShelfId") bookShelfId: Long,
		@Path("agonyId") agonyId: Long,
		@Path("recordId") recordId: Long,
	)

	@PUT("/v1/api/bookshelves/{bookShelfId}/agonies/{agonyId}/records/{recordId}")
	suspend fun reviseAgonyRecord(
		@Path("bookShelfId") bookShelfId: Long,
		@Path("agonyId") agonyId: Long,
		@Path("recordId") recordId: Long,
		@Body requestReviseAgonyRecord: RequestReviseAgonyRecord,
	): BookChatApiResult<Unit>

}