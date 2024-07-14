package com.example.bookchat.ui.bookshelf.complete

import com.example.bookchat.ui.bookshelf.model.BookShelfListItem


data class CompleteBookShelfUiState(
	val uiState: UiState,
	val completeItems: List<CompleteBookShelfItem>,
	val totalItemCount: Int,
) {

	val isEmptyReadingData: Boolean
		get() = completeItems.filterIsInstance<CompleteBookShelfItem.Item>().isEmpty()

	enum class UiState {
		SUCCESS,
		LOADING,
		ERROR,
		EMPTY,
	}

	companion object {
		val DEFAULT = CompleteBookShelfUiState(
			uiState = UiState.EMPTY,
			completeItems = emptyList(),
			totalItemCount = 0
		)
	}
}

sealed class CompleteBookShelfEvent {
	data class MoveToCompleteBookDialog(
		val bookShelfListItem: BookShelfListItem,
	) : CompleteBookShelfEvent()

	data class MakeToast(
		val stringId: Int,
	) : CompleteBookShelfEvent()
}

sealed interface CompleteBookShelfItem {

	fun getCategoryId(): Long {
		return when (this) {
			is Header -> HEADER_ITEM_STABLE_ID
			is Item -> bookShelfListItem.bookShelfId
		}
	}

	data class Header(val totalItemCount: Int) : CompleteBookShelfItem
	data class Item(val bookShelfListItem: BookShelfListItem) : CompleteBookShelfItem

	private companion object {
		private const val HEADER_ITEM_STABLE_ID = -1L
		private const val DUMMY_ITEM_STABLE_ID = -2L
	}
}