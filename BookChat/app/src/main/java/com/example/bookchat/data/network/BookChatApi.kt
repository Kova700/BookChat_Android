package com.example.bookchat.data.network

import com.example.bookchat.data.*
import com.example.bookchat.data.network.model.BookSearchSortOptionNetWork
import com.example.bookchat.data.network.model.BookSearchSortOptionNetWork.ACCURACY
import com.example.bookchat.data.network.model.SearchSortOptionNetwork
import com.example.bookchat.data.network.model.request.*
import com.example.bookchat.data.network.model.response.*
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface BookChatApi {
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
	)

	@POST("/v1/api/users/signin")
	suspend fun signIn(
		@Header("OIDC") idToken: String,
		@Body requestUserSignIn: RequestUserSignIn
	): Response<BookChatTokenResponse>

	@DELETE("/v1/api/users")
	suspend fun withdraw(
	)

	@GET("/v1/api/users/profile")
	suspend fun getUserProfile(
	): UserResponse

	/**------------도서------------*/

	@GET("/v1/api/books")
	suspend fun getBookSearchResult(
		@Query("query") query: String,
		@Query("size") size: Int,
		@Query("page") page: Int,
		@Query("sort") sort: BookSearchSortOptionNetWork = ACCURACY
	): ResponseGetBookSearch

	@POST("/v1/api/bookshelves")
	suspend fun registerBookShelfBook(
		@Body requestRegisterBookShelfBook: RequestRegisterBookShelfBook
	): Response<Unit>

	@GET("/v1/api/bookshelves")
	suspend fun getBookShelfItems(
		@Query("size") size: Int,
		@Query("page") page: Long,
		@Query("readingStatus") bookShelfState: String,
		@Query("sort") sort: SearchSortOptionNetwork = SearchSortOptionNetwork.UPDATED_AT_DESC
	): ResponseGetBookShelfBooks

	@DELETE("/v1/api/bookshelves/{bookId}")
	suspend fun deleteBookShelfBook(
		@Path("bookId") bookId: Long
	): Response<Unit>

	@PUT("/v1/api/bookshelves/{bookId}")
	suspend fun changeBookShelfBookStatus(
		@Path("bookId") bookId: Long,
		@Body requestChangeBookStatus: RequestChangeBookStatus
	)

	@GET("/v1/api/bookshelves/book")
	suspend fun checkAlreadyInBookShelf(
		@Query("isbn") isbn: String,
		@Query("publishAt") publishAt: String,
	): BookStateInBookShelfResponse

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
	)

	@GET("/v1/api/bookshelves/{bookShelfId}/agonies")
	suspend fun getAgony(
		@Path("bookShelfId") bookShelfId: Long,
		@Query("size") size: Int,
		@Query("sort") sort: SearchSortOptionNetwork,
		@Query("postCursorId") postCursorId: Long?,
	): ResponseGetAgony

	@DELETE("/v1/api/bookshelves/{bookShelfId}/agonies/{bookIdListString}")
	suspend fun deleteAgony(
		@Path("bookShelfId") bookShelfId: Long,
		@Path("bookIdListString") bookIdListString: String,
	)

	@PUT("/v1/api/bookshelves/{bookShelfId}/agonies/{agonyId}")
	suspend fun reviseAgony(
		@Path("bookShelfId") bookShelfId: Long,
		@Path("agonyId") agonyId: Long,
		@Body requestReviseAgony: RequestReviseAgony
	)

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
		@Query("postCursorId") postCursorId: Long?,
		@Query("size") size: Int,
		@Query("sort") sort: SearchSortOptionNetwork
	): ResponseGetAgonyRecord

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

	/**------------채팅방------------*/

	@GET("/v1/api/users/chatrooms")
	suspend fun getChannels(
		@Query("postCursorId") postCursorId: Long?,
		@Query("size") size: Int,
	): GetUserChannelResponse

	@Multipart
	@POST("/v1/api/chatrooms")
	suspend fun makeChannel(
		@Part("createChatRoomRequest") requestMakeChannel: RequestMakeChannel,
		@Part chatRoomImage: MultipartBody.Part? = null
	): Response<Unit>

	@POST("/v1/api/enter/chatrooms/{roomId}")
	suspend fun enterChatRoom(
		@Path("roomId") roomId: Long
	): Response<Unit>

	@DELETE("/v1/api/leave/chatrooms/{roomId}")
	suspend fun leaveChatRoom(
		@Path("roomId") roomId: Long
	): Response<Unit>

	@GET("/v1/api/chatrooms")
	suspend fun getSearchedChannels(
		@Query("postCursorId") postCursorId: Long?,
		@Query("size") size: Int,
		@Query("roomName") roomName: String?,
		@Query("title") title: String?,
		@Query("isbn") isbn: String?,
		@Query("tags") tags: String?,
	): ResponseGetChannelSearch

	@GET("/v1/api/chatrooms/{roomId}")
	suspend fun getChatRoomInfo(
		@Path("roomId") roomId: Long
	): ResponseChannelInfo

	/**------------채팅내역------------*/

	@GET("/v1/api/chatrooms/{roomId}/chats")
	suspend fun getChat(
		@Path("roomId") roomId: Long,
		@Query("size") size: Int,
		@Query("postCursorId") postCursorId: Long?,
		@Query("sort") sort: SearchSortOptionNetwork = SearchSortOptionNetwork.ID_DESC,
	): RespondGetChat
}