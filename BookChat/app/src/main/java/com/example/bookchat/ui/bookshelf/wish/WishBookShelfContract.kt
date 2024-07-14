package com.example.bookchat.ui.bookshelf.wish

import com.example.bookchat.domain.model.BookShelfState
import com.example.bookchat.ui.bookshelf.model.BookShelfListItem

data class WishBookShelfUiState(
	val uiState: UiState,
	val wishItems: List<WishBookShelfItem>,
) {
	val isEmptyWishData: Boolean
		get() = wishItems.filterIsInstance<WishBookShelfItem.Item>().isEmpty()

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
		val item: BookShelfListItem,
	) : WishBookShelfEvent()

	data class ChangeBookShelfTab(
		val targetState: BookShelfState,
	) : WishBookShelfEvent()

}

sealed interface WishBookShelfItem {

	fun getCategoryId(): Long {
		return when (this) {
			is Header -> HEADER_ITEM_STABLE_ID
			is Item -> bookShelfListItem.bookShelfId
			is Dummy -> hashCode().toLong()
		}
	}

	data class Header(val totalItemCount: Int) : WishBookShelfItem
	data class Item(val bookShelfListItem: BookShelfListItem) : WishBookShelfItem
	data class Dummy(val id: Int) : WishBookShelfItem

	private companion object {
		private const val HEADER_ITEM_STABLE_ID = -1L
		private const val DUMMY_ITEM_STABLE_ID = -2L
	}
}