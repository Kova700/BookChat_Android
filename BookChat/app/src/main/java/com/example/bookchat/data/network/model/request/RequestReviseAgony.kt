package com.example.bookchat.data.network.model.request

import com.example.bookchat.data.network.model.AgonyFolderHexColorNetwork
import com.google.gson.annotations.SerializedName

data class RequestReviseAgony(
	@SerializedName("title")
	val title: String,
	@SerializedName("hexColorCode")
	val hexColorCode: com.example.bookchat.data.network.model.AgonyFolderHexColorNetwork
)