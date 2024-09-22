package com.kova700.bookchat.core.network.bookchat.bookreport.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BookReportResponse(
	@SerialName("reportTitle")
	var reportTitle: String,
	@SerialName("reportContent")
	var reportContent: String,
	@SerialName("reportCreatedAt")
	val reportCreatedAt: String,
)
