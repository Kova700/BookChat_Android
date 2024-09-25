package com.kova700.bookchat.core.network.bookchat.client.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BookChatTokenResponse(
	@SerialName("accessToken")
	val accessToken: String,
	@SerialName("refreshToken")
	val refreshToken: String,
)
