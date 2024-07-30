package com.example.bookchat.ui.agony.makeagony

import com.example.bookchat.domain.model.AgonyFolderHexColor
import com.example.bookchat.domain.model.BookShelfItem

data class MakeAgonyUiState(
	val uiState: UiState,
	val bookshelfItem: BookShelfItem,
	val selectedColor: AgonyFolderHexColor,
	val agonyTitle: String,
) {
	enum class UiState {
		SUCCESS,
		LOADING,
		ERROR,
	}

	companion object {
		val DEFAULT = MakeAgonyUiState(
			uiState = UiState.LOADING,
			bookshelfItem = BookShelfItem.DEFAULT,
			selectedColor = AgonyFolderHexColor.WHITE,
			agonyTitle = ""
		)
	}
}

sealed class MakeAgonyUiEvent {
	data object MoveToBack : MakeAgonyUiEvent()

	data class ShowSnackBar(
		val stringId: Int,
	) : MakeAgonyUiEvent()
}
