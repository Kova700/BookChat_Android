package com.example.bookchat.data.response

import com.google.gson.annotations.SerializedName

data class ResponseGetAgonyRecord(
	@SerializedName("agonyRecordResponseList")
	val agonyRecordResponseList: List<AgonyRecordResponse>,
	@SerializedName("cursorMeta")
	val cursorMeta: CursorMeta
)
