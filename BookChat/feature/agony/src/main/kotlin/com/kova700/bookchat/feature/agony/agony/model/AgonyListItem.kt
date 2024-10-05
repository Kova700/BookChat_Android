package com.kova700.bookchat.feature.agony.agony.model

import com.kova700.bookchat.core.data.agony.external.model.AgonyFolderHexColor
import com.kova700.bookchat.core.data.bookshelf.external.model.BookShelfItem

sealed interface AgonyListItem {
	fun getCategoryId(): Long {
		return when (this) {
			is Header -> HEADER_ITEM_STABLE_ID
			is FirstItem -> FIRST_ITEM_STABLE_ID
			is Item -> agonyId
			PagingRetry -> PAGING_RETRY_STABLE_ID
		}
	}

	data class Header(val bookShelfItem: BookShelfItem) : AgonyListItem
	data object FirstItem : AgonyListItem
	data class Item(
		val agonyId: Long,
		val title: String,
		val hexColorCode: AgonyFolderHexColor,
		val isSelected: Boolean = false,
	) : AgonyListItem

	data object PagingRetry : AgonyListItem

	private companion object {
		private const val HEADER_ITEM_STABLE_ID = -1L
		private const val FIRST_ITEM_STABLE_ID = -2L
		private const val PAGING_RETRY_STABLE_ID = -3L
	}
}