package com.example.bookchat.ui.bookshelf.reading

import com.example.bookchat.domain.model.BookShelfState
import com.example.bookchat.ui.bookshelf.model.BookShelfListItem


data class ReadingBookShelfUiState(
	val uiState: UiState,
	val readingItems: List<ReadingBookShelfItem>,
) {

	enum class UiState {
		SUCCESS,
		LOADING,
		ERROR,
		EMPTY,
	}

	companion object {
		val DEFAULT = ReadingBookShelfUiState(
			uiState = UiState.EMPTY,
			readingItems = emptyList(),
		)
	}
}

sealed class ReadingBookShelfEvent {
	data class MoveToReadingBookDialog(
		val bookShelfListItem: BookShelfListItem
	) : ReadingBookShelfEvent()

	data class MoveToPageInputDialog(
		val bookShelfListItem: BookShelfListItem
	) : ReadingBookShelfEvent()

	data class ChangeBookShelfTab(
		val targetState: BookShelfState
	) : ReadingBookShelfEvent()

	data class MakeToast(
		val stringId: Int
	) : ReadingBookShelfEvent()

}

sealed interface ReadingBookShelfItem {

	fun getCategoryId(): Long {
		return when (this) {
			is Header -> HEADER_ITEM_STABLE_ID
			is Item -> bookShelfListItem.bookShelfId
		}
	}

	data class Header(val totalItemCount: Int) : ReadingBookShelfItem
	data class Item(val bookShelfListItem: BookShelfListItem) : ReadingBookShelfItem

	private companion object {
		private const val HEADER_ITEM_STABLE_ID = -1L
		private const val DUMMY_ITEM_STABLE_ID = -2L
	}
}