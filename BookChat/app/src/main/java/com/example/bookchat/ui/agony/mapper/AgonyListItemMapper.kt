package com.example.bookchat.ui.agony.mapper

import com.example.bookchat.domain.model.Agony
import com.example.bookchat.ui.agony.model.AgonyListItem

fun Agony.toAgonyListItem(isSelected: Boolean = false): AgonyListItem.Item {
	return AgonyListItem.Item(
		agonyId = agonyId,
		title = title,
		hexColorCode = hexColorCode,
		isSelected = isSelected
	)
}