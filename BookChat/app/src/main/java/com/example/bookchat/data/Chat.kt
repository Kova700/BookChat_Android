package com.example.bookchat.data

import com.google.gson.annotations.SerializedName

data class Chat(
	@SerializedName("chatId")
	val chatId: Long,
	@SerializedName("senderId")
	val senderId: Long?,
	@SerializedName("dispatchTime")
	val dispatchTime: String,
	@SerializedName("message")
	val message: String
)