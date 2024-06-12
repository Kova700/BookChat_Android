package com.example.bookchat.data.network.model.response

import com.google.gson.annotations.SerializedName

data class ChatScrapResponse(
	@SerializedName("scrapId")
	val scrapId: Long,
	@SerializedName("scrapContent")
	val scrapContent: String,
)