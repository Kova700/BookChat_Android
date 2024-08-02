package com.example.bookchat.ui.bookshelf.wish.dialog

import com.example.bookchat.domain.model.BookShelfItem
import com.example.bookchat.domain.model.BookShelfState

data class WishBookDialogUiState(
	val uiState: UiState,
	val wishItem: BookShelfItem,
	val isToggleChecked: Boolean,
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
			isToggleChecked = true
		)
	}
}

sealed class WishBookDialogEvent {
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