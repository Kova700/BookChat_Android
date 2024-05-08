package com.example.bookchat.ui.agony.model

import com.example.bookchat.domain.model.AgonyFolderHexColor
import com.example.bookchat.domain.model.BookShelfItem

sealed interface AgonyListItem {
	fun getCategoryId(): Long {
		return when (this) {
			is Header -> HEADER_ITEM_STABLE_ID
			is FirstItem -> FIRST_ITEM_STABLE_ID
			is Item -> agonyId
		}
	}

	data class Header(val bookShelfItem: BookShelfItem) : AgonyListItem
	object FirstItem : AgonyListItem
	data class Item(
		val agonyId: Long,
		val title: String,
		val hexColorCode: AgonyFolderHexColor,
		val isSelected: Boolean = false,
	) : AgonyListItem

	private companion object {
		private const val HEADER_ITEM_STABLE_ID = -1L
		private const val FIRST_ITEM_STABLE_ID = -2L
	}
}