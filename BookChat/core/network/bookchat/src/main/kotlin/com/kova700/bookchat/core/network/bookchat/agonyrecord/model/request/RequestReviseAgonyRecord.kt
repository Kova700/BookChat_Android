package com.kova700.bookchat.core.network.bookchat.agonyrecord.model.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RequestReviseAgonyRecord(
	@SerialName("recordTitle")
	val recordTitle: String,
	@SerialName("recordContent")
	val recordContent: String,
)
