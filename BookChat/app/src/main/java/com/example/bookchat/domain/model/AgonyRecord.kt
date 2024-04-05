package com.example.bookchat.domain.model

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