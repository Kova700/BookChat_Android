package com.example.bookchat.ui.bookshelf.reading.dialog

import com.example.bookchat.domain.model.BookShelfItem

data class PageInputDialogUiState(
	val uiState: UiState,
	val targetItem: BookShelfItem,
	val inputPage: String,
) {

	enum class UiState {
		SUCCESS,
		LOADING,
		ERROR,
	}

	companion object {
		val DEFAULT = PageInputDialogUiState(
			uiState = UiState.LOADING,
			targetItem = BookShelfItem.DEFAULT,
			inputPage = 0.toString(),
		)
	}
}

sealed class PageInputDialogEvent {
	data object CloseDialog : PageInputDialogEvent()
	data class ShowSnackBar(
		val stringId: Int,
	) : PageInputDialogEvent()
}