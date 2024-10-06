package com.kova700.bookchat.feature.agony.agonyrecord.model

import com.kova700.bookchat.core.data.agony.external.model.Agony

sealed interface AgonyRecordListItem {
	fun getCategoryId(): Long {
		return when (this) {
			is Header -> HEADER_ITEM_STABLE_ID
			is FirstItem -> FIRST_ITEM_STABLE_ID
			is Item -> recordId
			PagingError -> PAGING_ERROR_ITEM_STABLE_ID
		}
	}

	data class Header(
		val agony: Agony,
	) : AgonyRecordListItem

	data class FirstItem(
		val state: ItemState = ItemState.Success(),
	) : AgonyRecordListItem

	data class Item(
		val recordId: Long,
		val title: String,
		val content: String,
		val createdAt: String,
		val state: ItemState = ItemState.Success(),
	) : AgonyRecordListItem

	sealed interface ItemState {
		data class Success(val isSwiped: Boolean = false) : ItemState
		data object Loading : ItemState

		/** 타이핑 입력시 마다 새로운 ItemList submit함을 방지하기 위해 가변 프로퍼티 사용*/
		data class Editing(
			var editingTitle: String = "",
			var editingContent: String = "",
		) : ItemState
	}

	data object PagingError : AgonyRecordListItem

	companion object {
		const val HEADER_ITEM_STABLE_ID = -1L
		const val FIRST_ITEM_STABLE_ID = -2L
		const val PAGING_ERROR_ITEM_STABLE_ID = -3L
	}
}