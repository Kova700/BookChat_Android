package com.example.bookchat.ui.bookshelf.complete

import com.example.bookchat.ui.bookshelf.complete.model.CompleteBookShelfItem

data class CompleteBookShelfUiState(
	val uiState: UiState,
	val completeItems: List<CompleteBookShelfItem>,
) {
	val isLoading: Boolean
		get() = uiState == UiState.LOADING

	val isInitLoading: Boolean
		get() = uiState == UiState.INIT_LOADING

	val isSuccess: Boolean
		get() = uiState == UiState.SUCCESS
						&& isEmpty.not()

	val isEmpty: Boolean
		get() = completeItems.isEmpty()

	enum class UiState {
		SUCCESS,
		ERROR,
		LOADING,
		INIT_LOADING
	}

	companion object {
		val DEFAULT = CompleteBookShelfUiState(
			uiState = UiState.INIT_LOADING,
			completeItems = emptyList(),
		)
	}
}

sealed class CompleteBookShelfEvent {
	data class MoveToCompleteBookDialog(
		val bookShelfListItem: CompleteBookShelfItem.Item,
	) : CompleteBookShelfEvent()

	data class MakeToast(
		val stringId: Int,
	) : CompleteBookShelfEvent()
}