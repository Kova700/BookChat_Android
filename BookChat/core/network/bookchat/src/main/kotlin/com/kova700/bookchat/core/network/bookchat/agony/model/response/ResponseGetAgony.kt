package com.kova700.bookchat.core.network.bookchat.agony.model.response

import com.google.gson.annotations.SerializedName
import com.kova700.bookchat.core.network.bookchat.common.model.CursorMeta

data class ResponseGetAgony(
	@SerializedName("agonyResponseList")
	val agonyResponseList: List<AgonyResponse>,
	@SerializedName("cursorMeta")
	val cursorMeta: CursorMeta
)
