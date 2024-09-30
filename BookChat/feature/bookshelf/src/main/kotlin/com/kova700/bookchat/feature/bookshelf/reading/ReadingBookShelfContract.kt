package com.kova700.bookchat.feature.bookshelf.reading

import com.kova700.bookchat.core.data.bookshelf.external.model.BookShelfState
import com.kova700.bookchat.feature.bookshelf.reading.model.ReadingBookShelfItem

data class ReadingBookShelfUiState(
	val uiState: UiState,
	val readingItems: List<ReadingBookShelfItem>,
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
		get() = readingItems.isEmpty()
						&& isInitLoading.not()
						&& isInitError.not()

	val isNotEmpty: Boolean
		get() = readingItems.isNotEmpty()
						&& isInitLoading.not()
						&& isInitError.not()

	enum class UiState {
		SUCCESS,
		INIT_LOADING,
		INIT_ERROR,
		PAGING_LOADING,
		PAGING_ERROR
	}

	companion object {
		val DEFAULT = ReadingBookShelfUiState(
			uiState = UiState.SUCCESS,
			readingItems = emptyList(),
		)
	}
}

sealed class ReadingBookShelfEvent {
	data class MoveToReadingBookDialog(
		val bookShelfListItem: ReadingBookShelfItem.Item,
	) : ReadingBookShelfEvent()

	data class MoveToPageInputDialog(
		val bookShelfListItem: ReadingBookShelfItem.Item,
	) : ReadingBookShelfEvent()

	data class ChangeBookShelfTab(
		val targetState: BookShelfState,
	) : ReadingBookShelfEvent()

	data class ShowSnackBar(
		val stringId: Int,
	) : ReadingBookShelfEvent()

}