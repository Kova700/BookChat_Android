package com.example.bookchat.ui.bookshelf.complete.dialog

import com.example.bookchat.ui.bookshelf.model.BookShelfListItem

data class CompleteBookDialogUiState(
	val uiState: UiState,
	val completeItem: BookShelfListItem,
) {

	enum class UiState {
		SUCCESS,
		LOADING,
		ERROR,
		EMPTY,
	}

	companion object {
		val DEFAULT = CompleteBookDialogUiState(
			uiState = UiState.EMPTY,
			completeItem = BookShelfListItem.DEFAULT,
		)
	}
}

sealed class CompleteBookDialogEvent {
	data class MoveToAgony(
		val bookShelfItemId: Long
	) : CompleteBookDialogEvent()

	data class MoveToBookReport(
		val bookShelfItemId: Long
	) : CompleteBookDialogEvent()
}