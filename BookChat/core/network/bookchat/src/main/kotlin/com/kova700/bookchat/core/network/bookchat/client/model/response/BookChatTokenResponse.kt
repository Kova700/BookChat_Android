package com.kova700.bookchat.core.network.bookchat.client.model.response

import com.google.gson.annotations.SerializedName

data class BookChatTokenResponse(
	@SerializedName("accessToken")
	val accessToken: String,
	@SerializedName("refreshToken")
	val refreshToken: String
)
