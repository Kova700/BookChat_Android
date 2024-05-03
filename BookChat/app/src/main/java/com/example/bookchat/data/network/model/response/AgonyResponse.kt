package com.example.bookchat.data.network.model.response

import com.example.bookchat.data.network.model.AgonyFolderHexColorNetwork
import com.google.gson.annotations.SerializedName

data class AgonyResponse(
	@SerializedName("agonyId")
	val agonyId: Long,
	@SerializedName("title")
	val title: String,
	@SerializedName("hexColorCode")
	val hexColorCode: com.example.bookchat.data.network.model.AgonyFolderHexColorNetwork
)