package com.kova700.bookchat.core.network.bookchat.agony.model.response

import com.kova700.bookchat.core.network.bookchat.common.model.CursorMeta
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ResponseGetAgony(
	@SerialName("agonyResponseList")
	val agonyResponseList: List<AgonyResponse>,
	@SerialName("cursorMeta")
	val cursorMeta: CursorMeta,
)
