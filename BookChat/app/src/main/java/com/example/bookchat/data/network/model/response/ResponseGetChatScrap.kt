package com.example.bookchat.data.network.model.response

import com.google.gson.annotations.SerializedName

data class ResponseGetChatScrap(
	@SerializedName("scrapResponseList")
	val scrapResponseList: List<ChatScrapResponse>,
	@SerializedName("cursorMeta")
	val cursorMeta: CursorMeta,
)