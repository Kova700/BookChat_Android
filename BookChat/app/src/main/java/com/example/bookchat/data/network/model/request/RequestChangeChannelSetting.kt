package com.example.bookchat.data.network.model.request

import com.google.gson.annotations.SerializedName

data class RequestChangeChannelSetting(
	@SerializedName("roomId")
	val channelId: Long,
	@SerializedName("channelTitle")
	val channelTitle: String,
	@SerializedName("channelCapacity")
	val channelCapacity: Int,
	@SerializedName("channelTags")
	val channelTags: List<String>,
)
