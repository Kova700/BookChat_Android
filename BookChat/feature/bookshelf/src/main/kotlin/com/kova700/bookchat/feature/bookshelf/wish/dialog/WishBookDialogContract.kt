package com.kova700.bookchat.feature.bookshelf.wish.dialog

import com.kova700.bookchat.core.data.bookshelf.external.model.BookShelfItem
import com.kova700.bookchat.core.data.bookshelf.external.model.BookShelfState

data class WishBookDialogUiState(
	val uiState: UiState,
	val wishItem: BookShelfItem,
) {

	val isLoading
		get() = uiState == UiState.LOADING

	enum class UiState {
		SUCCESS,
		LOADING,
		ERROR,
	}

	companion object {
		val DEFAULT = WishBookDialogUiState(
			uiState = UiState.SUCCESS,
			wishItem = BookShelfItem.DEFAULT,
		)
	}
}

sealed class WishBookDialogEvent {
	data object MoveToBack : WishBookDialogEvent()
	data class ChangeBookShelfTab(
		val targetState: BookShelfState,
	) : WishBookDialogEvent()

	data class ShowSnackBar(
		val stringId: Int,
	) : WishBookDialogEvent()

	data class MoveToAgony(
		val bookShelfListItemId: Long,
	) : WishBookDialogEvent()
}