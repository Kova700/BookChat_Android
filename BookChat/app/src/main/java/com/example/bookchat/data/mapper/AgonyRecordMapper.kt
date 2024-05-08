package com.example.bookchat.data.mapper

import com.example.bookchat.data.network.model.response.AgonyRecordResponse
import com.example.bookchat.domain.model.AgonyRecord

fun com.example.bookchat.data.network.model.response.AgonyRecordResponse.toAgonyRecord(): AgonyRecord {
	return AgonyRecord(
		recordId = agonyRecordId,
		title = agonyRecordTitle,
		content = agonyRecordContent,
		createdAt = createdAt,
	)
}

fun List<com.example.bookchat.data.network.model.response.AgonyRecordResponse>.toAgonyRecord() =
	this.map { it.toAgonyRecord() }