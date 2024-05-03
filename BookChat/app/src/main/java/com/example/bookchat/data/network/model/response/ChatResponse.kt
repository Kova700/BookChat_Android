package com.example.bookchat.data.network.model.response

import com.google.gson.annotations.SerializedName

data class ChatResponse(
	@SerializedName("chatId")
	val chatId: Long,
	@SerializedName("senderId")
	val senderId: Long?,
	@SerializedName("dispatchTime")
	val dispatchTime: String,
	@SerializedName("message")
	val message: String
)