package com.example.bookchat.data.network.model.response

import com.google.gson.annotations.SerializedName

data class AgonyRecordResponse(
	@SerializedName("agonyRecordId")
	val agonyRecordId: Long,
	@SerializedName("agonyRecordTitle")
	val agonyRecordTitle: String,
	@SerializedName("agonyRecordContent")
	val agonyRecordContent: String,
	@SerializedName("createdAt")
	val createdAt: String
)