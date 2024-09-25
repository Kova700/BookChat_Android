package com.kova700.bookchat.core.datastore.bookchat_token.model

import kotlinx.serialization.Serializable

@Serializable
data class BookChatTokenEntity(
	val accessToken: String,
	val refreshToken: String,
)