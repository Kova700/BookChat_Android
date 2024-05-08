package com.example.bookchat.ui.bookshelf.wish.dialog

import com.example.bookchat.domain.model.BookShelfState
import com.example.bookchat.ui.bookshelf.model.BookShelfListItem

data class WishBookDialogUiState(
	val uiState: UiState,
	val wishItem: BookShelfListItem,
	val isToggleChecked: Boolean,
) {

	enum class UiState {
		SUCCESS,
		LOADING,
		ERROR,
		EMPTY,
	}

	companion object {
		val DEFAULT = WishBookDialogUiState(
			uiState = UiState.EMPTY,
			wishItem = BookShelfListItem.DEFAULT,
			isToggleChecked = true
		)
	}
}

sealed class WishBookDialogEvent {
	data class ChangeBookShelfTab(
		val targetState: BookShelfState
	) : WishBookDialogEvent()

	data class MakeToast(
		val stringId: Int
	) : WishBookDialogEvent()
}