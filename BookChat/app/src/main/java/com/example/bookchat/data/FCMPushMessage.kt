package com.example.bookchat.data

import com.google.gson.annotations.SerializedName

data class FCMPushMessage(
	@SerializedName("pushType")
	val pushType: PushType,
	@SerializedName("body")
	val chatId: Long,
)