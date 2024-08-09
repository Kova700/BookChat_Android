package com.example.bookchat.data.network

import com.example.bookchat.data.network.model.BookSearchSortOptionNetWork
import com.example.bookchat.data.network.model.BookSearchSortOptionNetWork.ACCURACY
import com.example.bookchat.data.network.model.ChannelMemberAuthorityNetwork
import com.example.bookchat.data.network.model.SearchSortOptionNetwork
import com.example.bookchat.data.network.model.request.RequestChangeBookStatus
import com.example.bookchat.data.network.model.request.RequestChangeChannelSetting
import com.example.bookchat.data.network.model.request.RequestChangeUserNickname
import com.example.bookchat.data.network.model.request.RequestMakeAgony
import com.example.bookchat.data.network.model.request.RequestMakeAgonyRecord
import com.example.bookchat.data.network.model.request.RequestMakeChannel
import com.example.bookchat.data.network.model.request.RequestRegisterBookReport
import com.example.bookchat.data.network.model.request.RequestRegisterBookShelfBook
import com.example.bookchat.data.network.model.request.RequestReviseAgony
import com.example.bookchat.data.network.model.request.RequestReviseAgonyRecord
import com.example.bookchat.data.network.model.request.RequestUserLogin
import com.example.bookchat.data.network.model.request.RequestUserSignUp
import com.example.bookchat.data.network.model.response.AgonyRecordResponse
import com.example.bookchat.data.network.model.response.AgonyResponse
import com.example.bookchat.data.network.model.response.BookChatTokenResponse
import com.example.bookchat.data.network.model.response.BookReportResponse
import com.example.bookchat.data.network.model.response.BookShelfItemResponse
import com.example.bookchat.data.network.model.response.BookStateInBookShelfResponse
import com.example.bookchat.data.network.model.response.ChannelSingleSearchResponse
import com.example.bookchat.data.network.model.response.GetUserChannelResponse
import com.example.bookchat.data.network.model.response.RespondGetChat
import com.example.bookchat.data.network.model.response.RespondGetChats
import com.example.bookchat.data.network.model.response.ResponseChannelInfo
import com.example.bookchat.data.network.model.response.ResponseGetAgony
import com.example.bookchat.data.network.model.response.ResponseGetAgonyRecord
import com.example.bookchat.data.network.model.response.ResponseGetBookSearch
import com.example.bookchat.data.network.model.response.ResponseGetBookShelfBooks
import com.example.bookchat.data.network.model.response.ResponseGetChannelSearch
import com.example.bookchat.data.network.model.response.UserResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface BookChatApi {
	//API 테스트
//    @POST("/2bc9634d-f24c-44ed-8215-d6e16121b8ae")

	/**------------유저------------*/

	@GET("/v1/api/users/profile/nickname")
	suspend fun requestNameDuplicateCheck(
		@Query("nickname") nickname: String,
	): Response<Unit>

	@Multipart
	@POST("/v1/api/users/signup")
	suspend fun signUp(
		@Header("OIDC") idToken: String,
		@Part userProfileImage: MultipartBody.Part? = null,
		@Part("userSignUpRequest") requestUserSignUp: RequestUserSignUp,
	)

	@PUT("/v1/api/devices/fcm-token")
	suspend fun renewFcmToken(
		@Body fcmToken: String,
	)

	@Multipart
	@POST("/v1/api/users/profile")
	suspend fun changeUserProfile(
		@Part userProfileImage: MultipartBody.Part? = null,
		@Part("changeUserNicknameRequest") requestChangeUserNickname: RequestChangeUserNickname,
	)

	@POST("/v1/api/users/signin")
	suspend fun login(
		@Header("OIDC") idToken: String,
		@Body requestUserLogin: RequestUserLogin,
	): Response<BookChatTokenResponse>

	@POST("/v1/api/users/logout")
	suspend fun logout()

	@POST("/v1/api/auth/token")
	suspend fun renewBookChatToken(
		@Body refreshToken: String,
	): BookChatTokenResponse

	@DELETE("/v1/api/users")
	suspend fun withdraw()

	@GET("/v1/api/users/profile")
	suspend fun getUserProfile(): UserResponse

	/**------------도서------------*/

	@GET("/v1/api/books")
	suspend fun getBookSearchResult(
		@Query("query") query: String,
		@Query("size") size: Int,
		@Query("page") page: Int,
		@Query("sort") sort: BookSearchSortOptionNetWork = ACCURACY,
	): ResponseGetBookSearch

	@POST("/v1/api/bookshelves")
	suspend fun registerBookShelfBook(
		@Body requestRegisterBookShelfBook: RequestRegisterBookShelfBook,
	): Response<Unit>

	@GET("/v1/api/bookshelves")
	suspend fun getBookShelfItems(
		@Query("size") size: Int,
		@Query("page") page: Long,
		@Query("readingStatus") bookShelfState: String,
		@Query("sort") sort: SearchSortOptionNetwork = SearchSortOptionNetwork.UPDATED_AT_DESC,
	): ResponseGetBookShelfBooks

	@GET("/v1/api/bookshelves/{bookShelfId}")
	suspend fun getBookShelfItem(
		@Path("bookShelfId") bookShelfId: Long,
	): BookShelfItemResponse

	@DELETE("/v1/api/bookshelves/{bookId}")
	suspend fun deleteBookShelfBook(
		@Path("bookId") bookId: Long,
	): Response<Unit>

	@PUT("/v1/api/bookshelves/{bookId}")
	suspend fun changeBookShelfBookStatus(
		@Path("bookId") bookId: Long,
		@Body requestChangeBookStatus: RequestChangeBookStatus,
	)

	@GET("/v1/api/bookshelves/book")
	suspend fun checkAlreadyInBookShelf(
		@Query("isbn") isbn: String,
		@Query("publishAt") publishAt: String,
	): Response<BookStateInBookShelfResponse>

	/**------------독후감------------*/

	@GET("/v1/api/bookshelves/{bookShelfId}/report")
	suspend fun getBookReport(
		@Path("bookShelfId") bookShelfId: Long,
	): Response<BookReportResponse>

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

	/**------------고민 ------------*/

	@POST("/v1/api/bookshelves/{bookId}/agonies")
	suspend fun makeAgony(
		@Path("bookId") bookId: Long,
		@Body requestMakeAgony: RequestMakeAgony,
	): Response<Unit>

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

	/**------------고민 기록------------*/

	@POST("/v1/api/bookshelves/{bookShelfId}/agonies/{agonyId}/records")
	suspend fun makeAgonyRecord(
		@Path("bookShelfId") bookShelfId: Long,
		@Path("agonyId") agonyId: Long,
		@Body requestMakeAgonyRecord: RequestMakeAgonyRecord,
	): Response<Unit>

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
	): Response<Unit>

	@PUT("/v1/api/bookshelves/{bookShelfId}/agonies/{agonyId}/records/{recordId}")
	suspend fun reviseAgonyRecord(
		@Path("bookShelfId") bookShelfId: Long,
		@Path("agonyId") agonyId: Long,
		@Path("recordId") recordId: Long,
		@Body requestReviseAgonyRecord: RequestReviseAgonyRecord,
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
		@Part chatRoomImage: MultipartBody.Part? = null,
		@Part("createChatRoomRequest") requestMakeChannel: RequestMakeChannel,
	): Response<Unit>

	@Multipart
	@POST("/v1/api/chatrooms/{roomId}")
	suspend fun changeChannelSetting(
		@Path("roomId") channelId: Long,
		@Part chatRoomImage: MultipartBody.Part? = null,
		@Part("reviseChatRoomRequest") requestChangeChannelSetting: RequestChangeChannelSetting,
	)

	@POST("/v1/api/enter/chatrooms/{roomId}")
	suspend fun enterChannel(
		@Path("roomId") roomId: Long,
	): Response<Unit>

	@DELETE("/v1/api/leave/chatrooms/{roomId}")
	suspend fun leaveChannel(
		@Path("roomId") roomId: Long,
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
	suspend fun getChannelInfo(
		@Path("roomId") roomId: Long,
	): ResponseChannelInfo

	/**------------채팅내역------------*/

	@GET("/v1/api/chatrooms/{roomId}/chats")
	suspend fun getChats(
		@Path("roomId") roomId: Long,
		@Query("size") size: Int,
		@Query("postCursorId") postCursorId: Long?,
		@Query("sort") sort: SearchSortOptionNetwork = SearchSortOptionNetwork.ID_DESC,
	): RespondGetChats

	@GET("/v1/api/chats/{chatId}")
	suspend fun getChat(
		@Path("chatId") chatId: Long,
	): RespondGetChat

	@GET("/v1/api/users/chatrooms/{roomId}")
	suspend fun getChannel(
		@Path("roomId") channelId: Long,
	): ChannelSingleSearchResponse

	@GET("/v1/api/members")
	suspend fun getUser(
		@Query("memberId") memberId: Long,
	): UserResponse

	@DELETE("/v1/api/chatrooms/{roomId}/participants/{userId}")
	suspend fun banChannelMember(
		@Path("roomId") channelId: Long,
		@Path("userId") userId: Long,
	)

	@PATCH("/v1/api/chatrooms/{roomId}/participants/{userId}")
	suspend fun updateChannelMemberAuthority(
		@Path("roomId") channelId: Long,
		@Path("userId") targetUserId: Long,
		@Query("participantStatus") authority: ChannelMemberAuthorityNetwork,
	)
}