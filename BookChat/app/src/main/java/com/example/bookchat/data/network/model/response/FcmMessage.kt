package com.example.bookchat.data.network.model.response

import com.example.bookchat.domain.model.PushType
import com.google.gson.annotations.SerializedName

data class FcmMessage(
	@SerializedName("pushType")
	val pushType: PushType,
	@SerializedName("body")
	val body: FcmBody,
)

data class FcmBody(
	@SerializedName("chatId")
	val chatId: Long,
	@SerializedName("chatRoomId")
	val channelId: Long,
)