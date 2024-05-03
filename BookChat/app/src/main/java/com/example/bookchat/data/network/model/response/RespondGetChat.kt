package com.example.bookchat.data.network.model.response

import com.google.gson.annotations.SerializedName

data class RespondGetChat(
	@SerializedName("chatResponseList")
    val chatResponseList: List<ChatResponse>,
	@SerializedName("cursorMeta")
    val cursorMeta: CursorMeta
)