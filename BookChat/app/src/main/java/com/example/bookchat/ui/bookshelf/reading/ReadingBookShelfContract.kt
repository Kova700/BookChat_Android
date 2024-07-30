package com.example.bookchat.ui.bookshelf.reading

import com.example.bookchat.domain.model.BookShelfState
import com.example.bookchat.ui.bookshelf.reading.model.ReadingBookShelfItem

data class ReadingBookShelfUiState(
	val uiState: UiState,
	val readingItems: List<ReadingBookShelfItem>,
) {
	val isLoading: Boolean
		get() = uiState == UiState.LOADING

	val isInitLoading: Boolean
		get() = uiState == UiState.INIT_LOADING

	val isEmpty: Boolean
		get() = readingItems.isEmpty()
						&& isLoading.not()
						&& isInitLoading.not()

	enum class UiState {
		SUCCESS,
		LOADING,
		INIT_LOADING
	}

	companion object {
		val DEFAULT = ReadingBookShelfUiState(
			uiState = UiState.INIT_LOADING,
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