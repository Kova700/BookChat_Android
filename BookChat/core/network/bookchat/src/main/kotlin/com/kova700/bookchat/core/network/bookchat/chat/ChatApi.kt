package com.kova700.bookchat.core.network.bookchat.chat

import com.kova700.bookchat.core.network.bookchat.common.model.SearchSortOptionNetwork
import com.kova700.bookchat.core.network.bookchat.chat.model.response.RespondGetChat
import com.kova700.bookchat.core.network.bookchat.chat.model.response.RespondGetChats
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ChatApi {
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

}