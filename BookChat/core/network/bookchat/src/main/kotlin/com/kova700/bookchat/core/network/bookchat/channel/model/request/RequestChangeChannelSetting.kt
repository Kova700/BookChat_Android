package com.kova700.bookchat.core.network.bookchat.channel.model.request

import com.google.gson.annotations.SerializedName

data class RequestChangeChannelSetting(
	@SerializedName("roomId")
	val channelId: Long,
	@SerializedName("roomName")
	val channelTitle: String,
	@SerializedName("roomSize")
	val channelCapacity: Int,
	@SerializedName("tags")
	val channelTags: List<String>,
)
