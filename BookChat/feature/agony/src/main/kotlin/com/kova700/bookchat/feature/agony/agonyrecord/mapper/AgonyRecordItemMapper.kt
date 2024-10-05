package com.kova700.bookchat.feature.agony.agonyrecord.mapper

import com.kova700.bookchat.core.data.agony.external.model.Agony
import com.kova700.bookchat.core.data.agonyrecord.external.model.AgonyRecord
import com.kova700.bookchat.feature.agony.agonyrecord.AgonyRecordUiState
import com.kova700.bookchat.feature.agony.agonyrecord.AgonyRecordUiState.UiState
import com.kova700.bookchat.feature.agony.agonyrecord.model.AgonyRecordListItem
import com.kova700.bookchat.feature.agony.agonyrecord.model.AgonyRecordListItem.Companion.FIRST_ITEM_STABLE_ID
import com.kova700.bookchat.feature.agony.agonyrecord.model.AgonyRecordListItem.ItemState

fun groupItems(
	records: List<AgonyRecord>,
	agony: Agony,
	stateMap: Map<Long, ItemState>,
	uiState: AgonyRecordUiState.UiState,
): List<AgonyRecordListItem> {
	val groupedItems = mutableListOf<AgonyRecordListItem>()
	groupedItems.add(AgonyRecordListItem.Header(agony))
	groupedItems.add(
		AgonyRecordListItem.FirstItem(stateMap[FIRST_ITEM_STABLE_ID] ?: ItemState.Success())
	)
	groupedItems.addAll(records.map {
		it.toAgonyRecordListItem(itemState = stateMap[it.recordId] ?: ItemState.Success())
	})
	if (uiState == UiState.PAGING_ERROR) groupedItems.add(AgonyRecordListItem.PagingError)
	return groupedItems
}

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