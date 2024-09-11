package com.kova700.bookchat.core.data.agonyrecord.external.model

data class AgonyRecord(
	val recordId: Long,
	val title: String,
	val content: String,
	val createdAt: String
){
	companion object{
		val DEFAULT = AgonyRecord(
			recordId = 0L,
			title = "",
			content = "",
			createdAt = ""
		)
	}
}