package com.example.bookchat.ui.bookshelf.wish

import com.example.bookchat.domain.model.BookShelfState
import com.example.bookchat.ui.bookshelf.model.BookShelfListItem

data class WishBookShelfUiState(
	val uiState: UiState,
	val wishItems: List<BookShelfListItem>,
	val totalItemCount :Int, //서버에 저장된 총 아이템 개수
) {

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
			totalItemCount = 0
		)
	}
}

sealed class WishBookShelfEvent {

	data class MoveToWishBookDialog(
		val item: BookShelfListItem
	) : WishBookShelfEvent()

	data class ChangeBookShelfTab(
		val targetState : BookShelfState
	) : WishBookShelfEvent()

}