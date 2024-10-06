package com.kova700.bookchat.feature.bookshelf.wish

import com.kova700.bookchat.core.data.bookshelf.external.model.BookShelfState
import com.kova700.bookchat.feature.bookshelf.wish.model.WishBookShelfItem

data class WishBookShelfUiState(
	val uiState: UiState,
	val wishItems: List<WishBookShelfItem>,
) {
	val isLoading: Boolean
		get() = isPagingLoading || isInitLoading

	val isPagingLoading: Boolean
		get() = uiState == UiState.PAGING_LOADING

	val isInitLoading: Boolean
		get() = uiState == UiState.INIT_LOADING

	val isInitError: Boolean
		get() = uiState == UiState.INIT_ERROR

	val isEmpty: Boolean
		get() = wishItems.isEmpty()
						&& isInitLoading.not()
						&& isInitError.not()

	val isNotEmpty: Boolean
		get() = wishItems.isNotEmpty()
						&& isInitLoading.not()
						&& isInitError.not()

	enum class UiState {
		SUCCESS,
		INIT_LOADING,
		INIT_ERROR,
		PAGING_LOADING,
		PAGING_ERROR
	}

	companion object {
		val DEFAULT = WishBookShelfUiState(
			uiState = UiState.SUCCESS,
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