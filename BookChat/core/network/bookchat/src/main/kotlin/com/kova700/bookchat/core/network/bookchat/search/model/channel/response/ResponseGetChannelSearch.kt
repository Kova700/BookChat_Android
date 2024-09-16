package com.kova700.bookchat.core.network.bookchat.search.model.channel.response

import com.google.gson.annotations.SerializedName
import com.kova700.bookchat.core.network.bookchat.common.model.CursorMeta

data class ResponseGetChannelSearch(
	@SerializedName("chatRoomResponseList")
	val channels: List<ChannelSearchResponse>,
	@SerializedName("cursorMeta")
	val cursorMeta: CursorMeta
)