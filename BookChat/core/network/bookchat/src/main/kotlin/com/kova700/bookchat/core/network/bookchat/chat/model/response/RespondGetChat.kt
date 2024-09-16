package com.kova700.bookchat.core.network.bookchat.chat.model.response

import com.google.gson.annotations.SerializedName
import com.kova700.bookchat.core.data.user.external.model.User

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
	val sender: User,
)