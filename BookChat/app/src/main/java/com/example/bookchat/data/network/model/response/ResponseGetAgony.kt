package com.example.bookchat.data.network.model.response

import com.google.gson.annotations.SerializedName

data class ResponseGetAgony(
	@SerializedName("agonyResponseList")
	val agonyResponseList: List<AgonyResponse>,
	@SerializedName("cursorMeta")
	val cursorMeta: CursorMeta
)
