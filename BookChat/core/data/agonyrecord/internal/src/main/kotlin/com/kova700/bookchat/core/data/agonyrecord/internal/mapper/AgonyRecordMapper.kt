package com.kova700.bookchat.core.data.agonyrecord.internal.mapper

import com.kova700.bookchat.core.data.agonyrecord.external.model.AgonyRecord
import com.kova700.bookchat.core.network.bookchat.model.response.AgonyRecordResponse

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