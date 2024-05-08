package com.example.bookchat.data

import com.example.bookchat.domain.model.PushType
import com.google.gson.annotations.SerializedName

data class FCMPushMessage(
	@SerializedName("pushType")
	val pushType: PushType,
	@SerializedName("body")
	val chatId: Long,
)