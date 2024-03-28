package com.example.bookchat.ui.bookshelf.reading.dialog

import com.example.bookchat.domain.model.BookShelfState
import com.example.bookchat.ui.bookshelf.model.BookShelfListItem

data class ReadingBookDialogUiState(
	val uiState: UiState,
	val readingItem: BookShelfListItem,
) {

	enum class UiState {
		SUCCESS,
		LOADING,
		ERROR,
		EMPTY,
	}

	companion object {
		val DEFAULT = ReadingBookDialogUiState(
			uiState = UiState.EMPTY,
			readingItem = BookShelfListItem.DEFAULT,
		)
	}
}

sealed class ReadingBookDialogEvent {
	data class MoveToAgony(val bookShelfListItemId: Long) : ReadingBookDialogEvent()
	data class ChangeBookShelfTab(
		val targetState: BookShelfState
	) : ReadingBookDialogEvent()
}