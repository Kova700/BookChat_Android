package com.example.bookchat.data.network.model.request

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
