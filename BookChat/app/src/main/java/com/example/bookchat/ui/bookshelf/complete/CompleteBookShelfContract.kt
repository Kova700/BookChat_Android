package com.example.bookchat.ui.bookshelf.complete

import com.example.bookchat.domain.model.Book
import com.example.bookchat.domain.model.BookShelfState
import com.example.bookchat.domain.model.StarRating
import java.util.Date

data class CompleteBookShelfUiState(
	val uiState: UiState,
	val completeItems: List<CompleteBookShelfItem>,
	val totalItemCount: Int,
) {

	val isEmptyData: Boolean
		get() = completeItems.filterIsInstance<CompleteBookShelfItem.Item>().isEmpty()

	val isLoading: Boolean
		get() = uiState == UiState.LOADING

	val isEmptyDataORLoading: Boolean
		get() = isEmptyData || isLoading

	enum class UiState {
		SUCCESS,
		LOADING,
		ERROR,
		EMPTY,
	}

	companion object {
		val DEFAULT = CompleteBookShelfUiState(
			uiState = UiState.EMPTY,
			completeItems = emptyList(),
			totalItemCount = 0
		)
	}
}

sealed class CompleteBookShelfEvent {
	data class MoveToCompleteBookDialog(
		val bookShelfListItem: CompleteBookShelfItem.Item,
	) : CompleteBookShelfEvent()

	data class MakeToast(
		val stringId: Int,
	) : CompleteBookShelfEvent()
}

sealed interface CompleteBookShelfItem {

	fun getCategoryId(): Long {
		return when (this) {
			is Header -> HEADER_ITEM_STABLE_ID
			is Item -> bookShelfId
		}
	}

	data class Header(val totalItemCount: Int) : CompleteBookShelfItem
	data class Item(
		val bookShelfId: Long,
		val book: Book,
		val pages: Int,
		val state: BookShelfState,
		val star: StarRating? = null,
		val lastUpdatedAt: Date,
		val isSwiped: Boolean = false,
	) : CompleteBookShelfItem

	private companion object {
		private const val HEADER_ITEM_STABLE_ID = -1L
		private const val DUMMY_ITEM_STABLE_ID = -2L
	}
}