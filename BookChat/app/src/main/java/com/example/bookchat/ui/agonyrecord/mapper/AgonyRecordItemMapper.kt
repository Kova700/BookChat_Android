package com.example.bookchat.ui.agonyrecord.mapper

import com.example.bookchat.domain.model.AgonyRecord
import com.example.bookchat.ui.agonyrecord.model.AgonyRecordListItem

fun AgonyRecord.toAgonyRecordListItem(
	itemState: AgonyRecordListItem.ItemState
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