package com.example.bookchat.ui.bookshelf.reading

import com.example.bookchat.domain.model.BookShelfState
import com.example.bookchat.ui.bookshelf.model.BookShelfListItem


data class ReadingBookShelfUiState(
	val uiState: UiState,
	val readingItems: List<BookShelfListItem>,
	val totalItemCount :Int, //서버에 저장된 총 아이템 개수
) {

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
			totalItemCount = 0
		)
	}
}

sealed class ReadingBookShelfEvent {
	data class MoveToReadingBookDialog(
		val bookShelfListItem: BookShelfListItem
	) : ReadingBookShelfEvent()

	data class MoveToPageInputDialog(
		val bookShelfListItem: BookShelfListItem
	) : ReadingBookShelfEvent()

	data class ChangeBookShelfTab(
		val targetState: BookShelfState
	) : ReadingBookShelfEvent()
}