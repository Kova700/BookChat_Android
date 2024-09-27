package com.kova700.bookchat.feature.bookshelf.complete

import com.kova700.bookchat.feature.bookshelf.complete.model.CompleteBookShelfItem

data class CompleteBookShelfUiState(
	val uiState: UiState,
	val completeItems: List<CompleteBookShelfItem>,
) {
	val isLoading: Boolean
		get() = uiState == UiState.LOADING

	val isInitLoading: Boolean
		get() = uiState == UiState.INIT_LOADING

	val isInitError: Boolean
		get() = uiState == UiState.INIT_ERROR

	val isEmpty: Boolean
		get() = completeItems.isEmpty()
						&& isLoading.not()
						&& isInitLoading.not()
						&& isInitError.not()

	val isNotEmpty: Boolean
		get() = completeItems.isNotEmpty()
						&& isLoading.not()
						&& isInitLoading.not()
						&& isInitError.not()

	enum class UiState {
		SUCCESS,
		LOADING,
		INIT_LOADING,
		INIT_ERROR,
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

	data class ShowSnackBar(
		val stringId: Int,
	) : CompleteBookShelfEvent()
}