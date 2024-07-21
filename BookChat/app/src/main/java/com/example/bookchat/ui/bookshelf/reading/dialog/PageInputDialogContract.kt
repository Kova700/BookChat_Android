package com.example.bookchat.ui.bookshelf.reading.dialog

import com.example.bookchat.domain.model.BookShelfItem

data class PageInputDialogUiState(
	val uiState: UiState,
	val targetItem: BookShelfItem,
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
			targetItem = BookShelfItem.DEFAULT,
		)
	}
}

sealed class PageInputDialogEvent {
	object CloseDialog : PageInputDialogEvent()
	data class MakeToast(
		val stringId: Int,
	) : PageInputDialogEvent()
}