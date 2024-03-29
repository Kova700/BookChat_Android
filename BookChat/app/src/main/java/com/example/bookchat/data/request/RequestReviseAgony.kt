package com.example.bookchat.data.request

import com.example.bookchat.data.model.AgonyFolderHexColorNetwork
import com.google.gson.annotations.SerializedName

data class RequestReviseAgony(
	@SerializedName("title")
	val title: String,
	@SerializedName("hexColorCode")
	val hexColorCode: AgonyFolderHexColorNetwork
)