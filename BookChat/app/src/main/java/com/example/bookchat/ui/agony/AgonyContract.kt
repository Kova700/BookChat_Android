package com.example.bookchat.ui.agony

import com.example.bookchat.ui.agony.model.AgonyListItem
import com.example.bookchat.ui.bookshelf.model.BookShelfListItem

data class AgonyUiState(
	val uiState: UiState,
	val bookshelfItem: BookShelfListItem,
	val agonies: List<AgonyListItem>
) {
	enum class UiState {
		SUCCESS,
		LOADING,
		ERROR,
		EMPTY,
		EDITING
	}

	companion object {
		val DEFAULT = AgonyUiState(
			uiState = UiState.LOADING,
			bookshelfItem = BookShelfListItem.DEFAULT,
			agonies = emptyList()
		)
	}
}

sealed class AgonyEvent {
	object MoveToBack : AgonyEvent()
	object ChangeItemViewMode : AgonyEvent()
	data class OpenBottomSheetDialog(val bookshelfItemId: Long) : AgonyEvent()

	data class MoveToAgonyRecord(
		val bookshelfItemId: Long,
		val agonyListItemId: Long
	) : AgonyEvent()
}
