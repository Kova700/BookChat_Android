package com.kova700.bookchat.feature.bookshelf.complete

import com.kova700.bookchat.feature.bookshelf.complete.model.CompleteBookShelfItem

data class CompleteBookShelfUiState(
	val uiState: UiState,
	val completeItems: List<CompleteBookShelfItem>,
) {
	val isLoading: Boolean
		get() = isPagingLoading || isInitLoading

	val isPagingLoading: Boolean
		get() = uiState == UiState.PAGING_LOADING

	val isInitLoading: Boolean
		get() = uiState == UiState.INIT_LOADING

	val isInitError: Boolean
		get() = uiState == UiState.INIT_ERROR

	val isEmpty: Boolean
		get() = completeItems.isEmpty()
						&& isInitLoading.not()
						&& isInitError.not()

	val isNotEmpty: Boolean
		get() = completeItems.isNotEmpty()
						&& isInitLoading.not()
						&& isInitError.not()

	enum class UiState {
		SUCCESS,
		INIT_LOADING,
		INIT_ERROR,
		PAGING_LOADING,
		PAGING_ERROR,
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