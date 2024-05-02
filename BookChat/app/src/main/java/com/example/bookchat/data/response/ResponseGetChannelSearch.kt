package com.example.bookchat.data.response

import com.google.gson.annotations.SerializedName

data class ResponseGetChannelSearch(
	@SerializedName("chatRoomResponseList")
	val channels: List<ChannelSearchResponse>,
	@SerializedName("cursorMeta")
	val cursorMeta: CursorMeta
)