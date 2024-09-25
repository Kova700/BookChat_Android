package com.kova700.bookchat.feature.agony.makeagony

import com.kova700.bookchat.core.data.agony.external.model.AgonyFolderHexColor
import com.kova700.bookchat.core.data.bookshelf.external.model.BookShelfItem

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
