package com.example.bookchat.data.response

import com.google.gson.annotations.SerializedName

data class GetUserChannelResponse(
	@SerializedName("userChatRoomResponseList")
    val channels :List<ChannelResponse>,
	@SerializedName("cursorMeta")
    val cursorMeta : CursorMeta
)