package com.kova700.bookchat.core.network.bookchat.agony

import com.kova700.bookchat.core.data.util.model.network.BookChatApiResult
import com.kova700.bookchat.core.network.bookchat.agony.model.request.RequestMakeAgony
import com.kova700.bookchat.core.network.bookchat.agony.model.request.RequestReviseAgony
import com.kova700.bookchat.core.network.bookchat.agony.model.response.AgonyResponse
import com.kova700.bookchat.core.network.bookchat.agony.model.response.ResponseGetAgony
import com.kova700.bookchat.core.network.bookchat.common.model.SearchSortOptionNetwork
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface AgonyApi {

	@POST("/v1/api/bookshelves/{bookId}/agonies")
	suspend fun makeAgony(
		@Path("bookId") bookId: Long,
		@Body requestMakeAgony: RequestMakeAgony,
	): BookChatApiResult<Unit>

	@GET("/v1/api/bookshelves/{bookShelfId}/agonies")
	suspend fun getAgonies(
		@Path("bookShelfId") bookShelfId: Long,
		@Query("size") size: Int,
		@Query("sort") sort: SearchSortOptionNetwork,
		@Query("postCursorId") postCursorId: Long?,
	): ResponseGetAgony

	@GET("/v1/api/bookshelves/{bookShelfId}/agonies/{agonyId}")
	suspend fun getAgony(
		@Path("bookShelfId") bookShelfId: Long,
		@Path("agonyId") agonyId: Long,
	): AgonyResponse

	@DELETE("/v1/api/bookshelves/{bookShelfId}/agonies/{bookIdListString}")
	suspend fun deleteAgony(
		@Path("bookShelfId") bookShelfId: Long,
		@Path("bookIdListString") bookIdListString: String,
	)

	@PUT("/v1/api/bookshelves/{bookShelfId}/agonies/{agonyId}")
	suspend fun reviseAgony(
		@Path("bookShelfId") bookShelfId: Long,
		@Path("agonyId") agonyId: Long,
		@Body requestReviseAgony: RequestReviseAgony,
	)

}