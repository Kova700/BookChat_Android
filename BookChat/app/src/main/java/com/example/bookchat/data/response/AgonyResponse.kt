package com.example.bookchat.data.response

import com.example.bookchat.data.model.AgonyFolderHexColorNetwork
import com.google.gson.annotations.SerializedName

data class AgonyResponse(
	@SerializedName("agonyId")
	val agonyId: Long,
	@SerializedName("title")
	val title: String,
	@SerializedName("hexColorCode")
	val hexColorCode: AgonyFolderHexColorNetwork
)