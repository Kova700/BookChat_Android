package com.kova700.bookchat.core.network.bookchat.channel.model.request

import com.kova700.bookchat.core.network.bookchat.channel.model.both.ChannelDefaultImageTypeNetwork
import com.kova700.bookchat.core.network.bookchat.common.model.BookRequest
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RequestMakeChannel(
	@SerialName("roomName")
	val roomName: String,
	@SerialName("roomSize")
	val roomSize: Int,
	@SerialName("defaultRoomImageType")
	val defaultRoomImageType: ChannelDefaultImageTypeNetwork,
	@SerialName("hashTags")
	val hashTags: List<String>,
	@SerialName("bookRequest")
	val bookRequest: BookRequest,
)