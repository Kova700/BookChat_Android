package com.example.bookchat.ui.bookshelf.reading

import com.example.bookchat.domain.model.BookShelfState
import com.example.bookchat.ui.bookshelf.reading.model.ReadingBookShelfItem


data class ReadingBookShelfUiState(
	val uiState: UiState,
	val readingItems: List<ReadingBookShelfItem>,
) {

	val isEmptyData: Boolean
		get() = isLoading.not()
						&& readingItems.filterIsInstance<ReadingBookShelfItem.Item>().isEmpty()

	val isLoading: Boolean
		get() = uiState == UiState.LOADING

	val isSuccess: Boolean
		get() = uiState == UiState.SUCCESS

	enum class UiState {
		SUCCESS,
		LOADING,
		ERROR,
		EMPTY,
	}

	companion object {
		val DEFAULT = ReadingBookShelfUiState(
			uiState = UiState.EMPTY,
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

	data class MakeToast(
		val stringId: Int,
	) : ReadingBookShelfEvent()

}