package com.kova700.bookchat.core.network.bookchat.channel.model.request

import com.google.gson.annotations.SerializedName
import com.kova700.bookchat.core.network.bookchat.channel.model.both.ChannelDefaultImageTypeNetwork
import com.kova700.bookchat.core.network.bookchat.common.model.BookRequest

data class RequestMakeChannel(
	@SerializedName("roomName")
	val roomName: String,
	@SerializedName("roomSize")
	val roomSize: Int,
	@SerializedName("defaultRoomImageType")
	val defaultRoomImageType: ChannelDefaultImageTypeNetwork,
	@SerializedName("hashTags")
	val hashTags: List<String>,
	@SerializedName("bookRequest")
	val bookRequest: BookRequest,
)