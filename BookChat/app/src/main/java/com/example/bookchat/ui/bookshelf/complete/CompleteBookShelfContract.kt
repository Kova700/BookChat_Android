package com.example.bookchat.ui.bookshelf.complete

import com.example.bookchat.ui.bookshelf.complete.model.CompleteBookShelfItem

data class CompleteBookShelfUiState(
	val uiState: UiState,
	val completeItems: List<CompleteBookShelfItem>,
	val totalItemCount: Int,
) {

	val isEmptyData: Boolean
		get() = isLoading.not()
						&& completeItems.filterIsInstance<CompleteBookShelfItem.Item>().isEmpty()

	val isLoading: Boolean
		get() = uiState == UiState.LOADING

	val isEmptyDataORLoading: Boolean
		get() = isEmptyData || isLoading

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
		val bookShelfListItem: CompleteBookShelfItem.Item,
	) : CompleteBookShelfEvent()

	data class MakeToast(
		val stringId: Int,
	) : CompleteBookShelfEvent()
}