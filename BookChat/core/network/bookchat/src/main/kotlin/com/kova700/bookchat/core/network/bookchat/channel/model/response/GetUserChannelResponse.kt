package com.kova700.bookchat.core.network.bookchat.channel.model.response

import com.google.gson.annotations.SerializedName
import com.kova700.bookchat.core.network.bookchat.common.model.CursorMeta

data class GetUserChannelResponse(
	@SerializedName("userChatRoomResponseList")
	val channels: List<ChannelResponse>,
	@SerializedName("cursorMeta")
	val cursorMeta: CursorMeta
)