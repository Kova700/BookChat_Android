package com.kova700.bookchat.core.network.bookchat.agony.model.response

import com.kova700.bookchat.core.network.bookchat.agony.model.both.AgonyFolderHexColorNetwork
import com.google.gson.annotations.SerializedName

data class AgonyResponse(
	@SerializedName("agonyId")
	val agonyId: Long,
	@SerializedName("title")
	val title: String,
	@SerializedName("hexColorCode")
	val hexColorCode: AgonyFolderHexColorNetwork,
)