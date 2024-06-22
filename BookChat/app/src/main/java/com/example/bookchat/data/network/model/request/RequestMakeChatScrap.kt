package com.example.bookchat.data.network.model.request

import com.google.gson.annotations.SerializedName

data class RequestMakeChatScrap(
	@SerializedName("bookShelfId")
	val bookShelfId: Long,
	@SerializedName("scrapContent")
	val scrapContent: String,
)