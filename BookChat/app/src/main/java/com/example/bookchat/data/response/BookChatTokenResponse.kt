package com.example.bookchat.data.response

import com.google.gson.annotations.SerializedName

data class BookChatTokenResponse(
	@SerializedName("accessToken")
	val accessToken: String,
	@SerializedName("refreshToken")
	val refreshToken: String
)
