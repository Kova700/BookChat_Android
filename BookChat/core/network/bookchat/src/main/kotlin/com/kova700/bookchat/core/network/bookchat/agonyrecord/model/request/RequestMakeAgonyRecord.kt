package com.kova700.bookchat.core.network.bookchat.agonyrecord.model.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RequestMakeAgonyRecord(
	@SerialName("title")
	val title: String,
	@SerialName("content")
	val content: String,
)
