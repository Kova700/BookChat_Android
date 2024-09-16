package com.kova700.bookchat.core.network.bookchat.chat.model.response

import com.google.gson.annotations.SerializedName
import com.kova700.bookchat.core.network.bookchat.common.model.CursorMeta

data class RespondGetChats(
	@SerializedName("chatResponseList")
    val chatResponseList: List<ChatResponse>,
	@SerializedName("cursorMeta")
    val cursorMeta: CursorMeta
)