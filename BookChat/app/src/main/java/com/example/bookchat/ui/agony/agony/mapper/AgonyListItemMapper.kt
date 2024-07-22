package com.example.bookchat.ui.agony.agony.mapper

import com.example.bookchat.domain.model.Agony
import com.example.bookchat.domain.model.BookShelfItem
import com.example.bookchat.ui.agony.agony.model.AgonyListItem

fun List<Agony>.toAgonyListItem(
	bookshelfItem: BookShelfItem,
	isSelectedMap: Map<Long, Boolean>,
): MutableList<AgonyListItem> {
	val groupedItems = mutableListOf<AgonyListItem>()
	groupedItems.add(AgonyListItem.Header(bookshelfItem))
	groupedItems.add(AgonyListItem.FirstItem)
	groupedItems.addAll(this.map {
		it.toAgonyListItem(isSelectedMap[it.agonyId] ?: false)
	})
	return groupedItems
}

fun Agony.toAgonyListItem(isSelected: Boolean = false): AgonyListItem.Item {
	return AgonyListItem.Item(
		agonyId = agonyId,
		title = title,
		hexColorCode = hexColorCode,
		isSelected = isSelected
	)
}