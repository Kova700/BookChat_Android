package com.example.bookchat.ui.bookshelf.complete

import com.example.bookchat.ui.bookshelf.model.BookShelfListItem


data class CompleteBookShelfUiState(
	val uiState: UiState,
	val completeItems: List<BookShelfListItem>,
	val totalItemCount :Int, //서버에 저장된 총 아이템 개수
) {

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
		val bookShelfListItem: BookShelfListItem
	) : CompleteBookShelfEvent()
}