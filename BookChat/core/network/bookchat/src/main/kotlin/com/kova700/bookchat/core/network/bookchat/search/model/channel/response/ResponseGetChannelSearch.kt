package com.kova700.bookchat.core.network.bookchat.search.model.channel.response

import com.kova700.bookchat.core.network.bookchat.common.model.CursorMeta
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ResponseGetChannelSearch(
	@SerialName("chatRoomResponseList")
	val channels: List<ChannelSearchResponse>,
	@SerialName("cursorMeta")
	val cursorMeta: CursorMeta,
)