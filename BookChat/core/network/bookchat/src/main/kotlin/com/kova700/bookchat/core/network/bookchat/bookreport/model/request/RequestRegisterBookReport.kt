package com.kova700.bookchat.core.network.bookchat.bookreport.model.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RequestRegisterBookReport(
	@SerialName("title")
	val title: String,
	@SerialName("content")
	val content: String,
)
