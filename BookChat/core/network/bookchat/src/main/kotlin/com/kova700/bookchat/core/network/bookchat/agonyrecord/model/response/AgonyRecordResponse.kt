package com.kova700.bookchat.core.network.bookchat.agonyrecord.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AgonyRecordResponse(
	@SerialName("agonyRecordId")
	val agonyRecordId: Long,
	@SerialName("agonyRecordTitle")
	val agonyRecordTitle: String,
	@SerialName("agonyRecordContent")
	val agonyRecordContent: String,
	@SerialName("createdAt")
	val createdAt: String,
)