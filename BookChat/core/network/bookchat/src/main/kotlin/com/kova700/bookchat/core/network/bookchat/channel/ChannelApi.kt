package com.kova700.bookchat.core.network.bookchat.channel

import com.kova700.bookchat.core.data.common.model.network.BookChatApiResult
import com.kova700.bookchat.core.network.bookchat.channel.model.both.ChannelMemberAuthorityNetwork
import com.kova700.bookchat.core.network.bookchat.channel.model.request.RequestChangeChannelSetting
import com.kova700.bookchat.core.network.bookchat.channel.model.request.RequestMakeChannel
import com.kova700.bookchat.core.network.bookchat.channel.model.response.ChannelSingleSearchResponse
import com.kova700.bookchat.core.network.bookchat.channel.model.response.GetUserChannelResponse
import com.kova700.bookchat.core.network.bookchat.channel.model.response.ResponseChannelInfo
import okhttp3.MultipartBody
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ChannelApi {

	@GET("/v1/api/users/chatrooms")
	suspend fun getChannels(
		@Query("postCursorId") postCursorId: Long?,
		@Query("size") size: Int,
	): GetUserChannelResponse

	@GET("/v1/api/users/chatrooms/{roomId}")
	suspend fun getChannel(
		@Path("roomId") channelId: Long,
	): ChannelSingleSearchResponse

	@Multipart
	@POST("/v1/api/chatrooms")
	suspend fun makeChannel(
		@Part chatRoomImage: MultipartBody.Part? = null,
		@Part("createChatRoomRequest") requestMakeChannel: RequestMakeChannel,
	): BookChatApiResult<Unit>

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
	): BookChatApiResult<Unit>

	@DELETE("/v1/api/leave/chatrooms/{roomId}")
	suspend fun leaveChannel(
		@Path("roomId") roomId: Long,
	): BookChatApiResult<Unit>

	@GET("/v1/api/chatrooms/{roomId}")
	suspend fun getChannelInfo(
		@Path("roomId") roomId: Long,
	): BookChatApiResult<ResponseChannelInfo>

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
