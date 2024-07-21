package com.example.bookchat.ui.bookshelf.reading.dialog

import com.example.bookchat.domain.model.BookShelfItem
import com.example.bookchat.domain.model.BookShelfState

data class ReadingBookDialogUiState(
	val uiState: UiState,
	val readingItem: BookShelfItem,
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
			readingItem = BookShelfItem.DEFAULT,
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