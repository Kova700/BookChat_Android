package com.kova700.bookchat.feature.bookshelf

import com.kova700.bookchat.core.data.bookshelf.external.model.BookShelfState

data class BookShelfUiState(
	val uiState: UiState,
) {

	enum class UiState {
		SUCCESS,
		LOADING,
		ERROR,
		EMPTY,
	}

	companion object {
		val DEFAULT = BookShelfUiState(
			uiState = UiState.EMPTY,
		)
	}
}

sealed class BookShelfEvent {
	data class ChangeBookShelfTab(val targetState: BookShelfState) : BookShelfEvent()
}