package com.example.bookchat.data.network.model.response

import com.example.bookchat.domain.model.User
import com.google.gson.annotations.SerializedName

data class RespondGetChat(
	@SerializedName("chatId")
	val chatId: Long,
	@SerializedName("chatRoomId")
	val channelId: Long,
	@SerializedName("message")
	val message: String,
	@SerializedName("dispatchTime")
	val dispatchTime: String,
	@SerializedName("sender")
	val sender: User
)