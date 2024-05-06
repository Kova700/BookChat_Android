package com.example.bookchat.ui.search.dialog

import com.example.bookchat.domain.model.Book
import com.example.bookchat.domain.model.BookShelfState

data class SearchDialogUiState(
	val uiState: SearchDialogState,
	val book: Book,
	val starRating: Float,
) {

	sealed class SearchDialogState {
		object Default : SearchDialogState()
		object Loading : SearchDialogState()
		data class AlreadyInBookShelf(val bookShelfState: BookShelfState) : SearchDialogState()
	}

	companion object {
		val DEFAULT = SearchDialogUiState(
			uiState = SearchDialogState.Loading,
			book = Book.DEFAULT,
			starRating = 0F,
		)
	}
}

sealed class SearchTapDialogEvent {
	object MoveToStarSetDialog : SearchTapDialogEvent()
	data class MakeToast(
		val stringId: Int
	) : SearchTapDialogEvent()
}
