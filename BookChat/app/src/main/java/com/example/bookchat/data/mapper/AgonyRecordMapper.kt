package com.example.bookchat.data.mapper

import com.example.bookchat.data.response.AgonyRecordResponse
import com.example.bookchat.domain.model.AgonyRecord

fun AgonyRecordResponse.toAgonyRecord(): AgonyRecord {
	return AgonyRecord(
		recordId = agonyRecordId,
		title = agonyRecordTitle,
		content = agonyRecordContent,
		createdAt = createdAt,
	)
}

fun List<AgonyRecordResponse>.toAgonyRecord() =
	this.map { it.toAgonyRecord() }