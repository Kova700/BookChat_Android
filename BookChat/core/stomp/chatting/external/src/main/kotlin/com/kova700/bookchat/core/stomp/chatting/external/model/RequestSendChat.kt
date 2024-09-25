package com.kova700.bookchat.core.stomp.chatting.external.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RequestSendChat(
	@SerialName("receiptId")
	val receiptId: Long,
	@SerialName("message")
	val message: String,
)