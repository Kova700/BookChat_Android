package com.kova700.bookchat.core.network.bookchat.agony.model.request

import com.kova700.bookchat.core.network.bookchat.agony.model.both.AgonyFolderHexColorNetwork
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RequestMakeAgony(
	@SerialName("title")
	val title: String,
	@SerialName("hexColorCode")
	val hexColorCode: AgonyFolderHexColorNetwork,
)
