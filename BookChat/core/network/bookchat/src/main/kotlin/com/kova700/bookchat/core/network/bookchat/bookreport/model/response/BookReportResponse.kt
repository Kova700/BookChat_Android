package com.kova700.bookchat.core.network.bookchat.bookreport.model.response

import com.google.gson.annotations.SerializedName

data class BookReportResponse(
	@SerializedName("reportTitle")
	var reportTitle: String,
	@SerializedName("reportContent")
	var reportContent: String,
	@SerializedName("reportCreatedAt")
	val reportCreatedAt: String
)
