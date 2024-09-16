package com.kova700.bookchat.core.network.bookchat.agonyrecord.model.response

import com.google.gson.annotations.SerializedName
import com.kova700.bookchat.core.network.bookchat.common.model.CursorMeta

data class ResponseGetAgonyRecord(
	@SerializedName("agonyRecordResponseList")
	val agonyRecordResponseList: List<AgonyRecordResponse>,
	@SerializedName("cursorMeta")
	val cursorMeta: CursorMeta
)
