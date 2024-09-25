package com.kova700.bookchat.core.network.bookchat.channel.model.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RequestChangeChannelSetting(
	@SerialName("roomId")
	val channelId: Long,
	@SerialName("roomName")
	val channelTitle: String,
	@SerialName("roomSize")
	val channelCapacity: Int,
	@SerialName("tags")
	val channelTags: List<String>,
)
