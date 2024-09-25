package com.kova700.bookchat.core.fcm.service.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FcmMessage(
	@SerialName("pushType") val pushType: PushType,
	@SerialName("body") val body: FcmBody? = null,
)

@Serializable
data class FcmBody(
	@SerialName("chatId")
	val chatId: Long,
	@SerialName("chatRoomId")
	val channelId: Long,
)