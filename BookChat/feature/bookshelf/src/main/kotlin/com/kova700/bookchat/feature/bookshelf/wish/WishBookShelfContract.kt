package com.kova700.bookchat.feature.bookshelf.wish

import com.kova700.bookchat.core.data.bookshelf.external.model.BookShelfState
import com.kova700.bookchat.feature.bookshelf.wish.model.WishBookShelfItem

data class WishBookShelfUiState(
	val uiState: UiState,
	val wishItems: List<WishBookShelfItem>,
) {
	val isLoading: Boolean
		get() = uiState == UiState.LOADING

	val isInitLoading: Boolean
		get() = uiState == UiState.INIT_LOADING

	val isEmpty: Boolean
		get() = wishItems.isEmpty()
						&& isLoading.not()
						&& isInitLoading.not()

	enum class UiState {
		SUCCESS,
		LOADING,
		INIT_LOADING
	}

	companion object {
		val DEFAULT = WishBookShelfUiState(
			uiState = UiState.INIT_LOADING,
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

	data class ShowSnackBar(
		val stringId: Int,
	) : WishBookShelfEvent()

}