package com.kova700.bookchat.core.network.bookchat.chat.model.response

import com.kova700.bookchat.core.network.bookchat.common.model.CursorMeta
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RespondGetChats(
	@SerialName("chatResponseList")
	val chatResponseList: List<ChatResponse>,
	@SerialName("cursorMeta")
	val cursorMeta: CursorMeta,
)