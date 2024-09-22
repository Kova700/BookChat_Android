package com.kova700.bookchat.core.network.bookchat.chat.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChatResponse(
	@SerialName("chatId")
	val chatId: Long,
	@SerialName("senderId")
	val senderId: Long?,
	@SerialName("dispatchTime")
	val dispatchTime: String,
	@SerialName("message")
	val message: String,
)