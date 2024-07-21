package com.example.bookchat.ui.bookshelf.wish

import com.example.bookchat.domain.model.BookShelfState
import com.example.bookchat.ui.bookshelf.wish.model.WishBookShelfItem

data class WishBookShelfUiState(
	val uiState: UiState,
	val wishItems: List<WishBookShelfItem>,
) {
	val isEmptyData: Boolean
		get() = isLoading.not()
						&& wishItems.filterIsInstance<WishBookShelfItem.Item>().isEmpty()

	val isLoading: Boolean
		get() = uiState == WishBookShelfUiState.UiState.LOADING

	val isEmptyDataORLoading: Boolean
		get() = isEmptyData || isLoading

	enum class UiState {
		SUCCESS,
		LOADING,
		ERROR,
		EMPTY,
	}

	companion object {
		val DEFAULT = WishBookShelfUiState(
			uiState = UiState.EMPTY,
			wishItems = emptyList(),
		)
	}
}

sealed class WishBookShelfEvent {

	data class MoveToWishBookDialog(
		val item: WishBookShelfItem.Item,
	) : WishBookShelfEvent()

	data class ChangeBookShelfTab(
		val targetState: BookShelfState,
	) : WishBookShelfEvent()

}