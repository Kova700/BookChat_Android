package com.example.bookchat.ui.bookshelf.reading.dialog

import com.example.bookchat.domain.model.BookShelfState
import com.example.bookchat.ui.bookshelf.model.BookShelfListItem

data class ReadingBookDialogUiState(
	val uiState: UiState,
	val readingItem: BookShelfListItem,
	val starRating: Float,
) {
	val haveStar
		get() = starRating != 0F

	enum class UiState {
		SUCCESS,
		LOADING,
		ERROR,
		EMPTY,
	}

	companion object {
		val DEFAULT = ReadingBookDialogUiState(
			uiState = UiState.EMPTY,
			readingItem = BookShelfListItem.DEFAULT,
			starRating = 0.0f
		)
	}
}

sealed class ReadingBookDialogEvent {
	data class MoveToAgony(
		val bookShelfListItemId: Long,
	) : ReadingBookDialogEvent()

	data class ChangeBookShelfTab(
		val targetState: BookShelfState,
	) : ReadingBookDialogEvent()

	data class MakeToast(
		val stringId: Int,
	) : ReadingBookDialogEvent()
}