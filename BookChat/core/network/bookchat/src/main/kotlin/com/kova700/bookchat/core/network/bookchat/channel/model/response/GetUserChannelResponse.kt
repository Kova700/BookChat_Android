package com.kova700.bookchat.core.network.bookchat.channel.model.response

import com.kova700.bookchat.core.network.bookchat.common.model.CursorMeta
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetUserChannelResponse(
	@SerialName("userChatRoomResponseList")
	val channels: List<ChannelResponse>,
	@SerialName("cursorMeta")
	val cursorMeta: CursorMeta,
)