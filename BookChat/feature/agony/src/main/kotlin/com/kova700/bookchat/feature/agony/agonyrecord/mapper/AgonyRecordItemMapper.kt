package com.kova700.bookchat.feature.agony.agonyrecord.mapper

import com.kova700.bookchat.core.data.agonyrecord.external.model.AgonyRecord
import com.kova700.bookchat.feature.agony.agonyrecord.model.AgonyRecordListItem

fun AgonyRecord.toAgonyRecordListItem(
	itemState: AgonyRecordListItem.ItemState,
): AgonyRecordListItem.Item {
	return AgonyRecordListItem.Item(
		recordId = recordId,
		title = title,
		content = content,
		createdAt = createdAt,
		state = itemState,
	)
}

fun AgonyRecordListItem.Item.toAgonyRecord(): AgonyRecord {
	return AgonyRecord(
		recordId = recordId,
		title = title,
		content = content,
		createdAt = createdAt,
	)
}