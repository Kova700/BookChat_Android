package com.kova700.bookchat.core.network.bookchat.agony.model.response

import com.kova700.bookchat.core.network.bookchat.agony.model.both.AgonyFolderHexColorNetwork
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AgonyResponse(
	@SerialName("agonyId")
	val agonyId: Long,
	@SerialName("title")
	val title: String,
	@SerialName("hexColorCode")
	val hexColorCode: AgonyFolderHexColorNetwork,
)