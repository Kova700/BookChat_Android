package com.kova700.bookchat.feature.agony.agony

import com.kova700.bookchat.core.data.bookshelf.external.model.BookShelfItem
import com.kova700.bookchat.feature.agony.agony.model.AgonyListItem

data class AgonyUiState(
	val uiState: UiState,
	val bookshelfItem: BookShelfItem,
	val agonies: List<AgonyListItem>,
) {
	enum class UiState {
		SUCCESS,
		LOADING,
		INIT_LOADING,
		EDITING
	}

	companion object {
		val DEFAULT = AgonyUiState(
			uiState = UiState.INIT_LOADING,
			bookshelfItem = BookShelfItem.DEFAULT,
			agonies = emptyList()
		)
	}
}

sealed class AgonyEvent {
	data object MoveToBack : AgonyEvent()
	data object RenewItemViewMode : AgonyEvent()
	data class OpenBottomSheetDialog(val bookshelfItemId: Long) : AgonyEvent()

	data class MoveToAgonyRecord(
		val bookshelfItemId: Long,
		val agonyListItemId: Long,
	) : AgonyEvent()

	data class ShowSnackBar(
		val stringId: Int,
	) : AgonyEvent()

}
