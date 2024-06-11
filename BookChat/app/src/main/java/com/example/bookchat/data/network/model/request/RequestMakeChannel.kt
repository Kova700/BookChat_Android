package com.example.bookchat.data.network.model.request

import com.example.bookchat.data.network.model.ChannelDefaultImageTypeNetwork
import com.google.gson.annotations.SerializedName

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