package com.example.bookchat.data.network.model.response

import com.google.gson.annotations.SerializedName

data class GetUserChannelResponse(
	@SerializedName("userChatRoomResponseList")
    val channels :List<com.example.bookchat.data.network.model.response.ChannelResponse>,
	@SerializedName("cursorMeta")
    val cursorMeta : CursorMeta
)