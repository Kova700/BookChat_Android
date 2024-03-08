package com.example.bookchat.ui.bookshelf.reading.dialog

import com.example.bookchat.ui.bookshelf.model.BookShelfListItem

data class PageInputDialogUiState(
	val uiState: UiState,
	val targetItem: BookShelfListItem,
) {

	enum class UiState {
		SUCCESS,
		LOADING,
		ERROR,
		EMPTY,
	}

	companion object {
		val DEFAULT = PageInputDialogUiState(
			uiState = UiState.EMPTY,
			targetItem = BookShelfListItem.DEFAULT,
		)
	}
}

sealed class PageInputDialogEvent {
	object CloseDialog : PageInputDialogEvent()
}