package com.kova700.bookchat.feature.agony.agony

import com.kova700.bookchat.core.data.bookshelf.external.model.BookShelfItem
import com.kova700.bookchat.feature.agony.agony.model.AgonyListItem

data class AgonyUiState(
	val uiState: UiState,
	val bookshelfItem: BookShelfItem,
	val agonies: List<AgonyListItem>,
) {
	val isNotInitLoadingOrError: Boolean
		get() = (isInitLoading || isInitError).not()

	val isInitLoading: Boolean
		get() = uiState == UiState.INIT_LOADING

	val isPagingLoading: Boolean
		get() = uiState == UiState.PAGING_LOADING

	val isLoading: Boolean
		get() = isInitLoading || isPagingLoading

	val isInitError: Boolean
		get() = uiState == UiState.INIT_ERROR

	val isEditing: Boolean
		get() = uiState == UiState.EDITING


	enum class UiState {
		SUCCESS,
		INIT_LOADING,
		INIT_ERROR,
		PAGING_LOADING,
		PAGING_ERROR,
		EDITING
	}

	companion object {
		val DEFAULT = AgonyUiState(
			uiState = UiState.SUCCESS,
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
