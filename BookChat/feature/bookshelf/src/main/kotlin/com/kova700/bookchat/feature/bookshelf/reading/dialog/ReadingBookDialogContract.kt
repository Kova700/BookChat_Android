package com.kova700.bookchat.feature.bookshelf.reading.dialog

import com.kova700.bookchat.core.data.bookshelf.external.model.BookShelfItem
import com.kova700.bookchat.core.data.bookshelf.external.model.BookShelfState

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
	}

	companion object {
		val DEFAULT = ReadingBookDialogUiState(
			uiState = UiState.SUCCESS,
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

	data class ShowSnackBar(
		val stringId: Int,
	) : ReadingBookDialogEvent()
}