package com.kova700.bookchat.core.network.bookchat.agonyrecord.model.response

import com.kova700.bookchat.core.network.bookchat.common.model.CursorMeta
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ResponseGetAgonyRecord(
	@SerialName("agonyRecordResponseList")
	val agonyRecordResponseList: List<AgonyRecordResponse>,
	@SerialName("cursorMeta")
	val cursorMeta: CursorMeta,
)
