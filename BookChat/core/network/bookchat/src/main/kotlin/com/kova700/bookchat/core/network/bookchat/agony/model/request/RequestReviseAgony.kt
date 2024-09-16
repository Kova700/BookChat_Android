package com.kova700.bookchat.core.network.bookchat.agony.model.request

import com.kova700.bookchat.core.network.bookchat.agony.model.both.AgonyFolderHexColorNetwork
import com.google.gson.annotations.SerializedName

data class RequestReviseAgony(
	@SerializedName("title")
	val title: String,
	@SerializedName("hexColorCode")
	val hexColorCode: AgonyFolderHexColorNetwork,
)