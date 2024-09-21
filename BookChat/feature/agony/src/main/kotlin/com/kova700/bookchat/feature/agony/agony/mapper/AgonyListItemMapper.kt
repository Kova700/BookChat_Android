package com.kova700.bookchat.feature.agony.agony.mapper

import com.kova700.bookchat.core.data.agony.external.model.Agony
import com.kova700.bookchat.core.data.bookshelf.external.model.BookShelfItem
import com.kova700.bookchat.feature.agony.agony.model.AgonyListItem

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