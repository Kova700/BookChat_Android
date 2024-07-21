package com.example.bookchat.ui.bookshelf.reading

import com.example.bookchat.domain.model.Book
import com.example.bookchat.domain.model.BookShelfState
import com.example.bookchat.domain.model.StarRating
import java.util.Date


data class ReadingBookShelfUiState(
	val uiState: UiState,
	val readingItems: List<ReadingBookShelfItem>,
) {

	val isEmptyData: Boolean
		get() = isLoading.not()
						&& readingItems.filterIsInstance<ReadingBookShelfItem.Item>().isEmpty()

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
		val DEFAULT = ReadingBookShelfUiState(
			uiState = UiState.EMPTY,
			readingItems = emptyList(),
		)
	}
}

sealed class ReadingBookShelfEvent {
	data class MoveToReadingBookDialog(
		val bookShelfListItem: ReadingBookShelfItem.Item,
	) : ReadingBookShelfEvent()

	data class MoveToPageInputDialog(
		val bookShelfListItem: ReadingBookShelfItem.Item,
	) : ReadingBookShelfEvent()

	data class ChangeBookShelfTab(
		val targetState: BookShelfState,
	) : ReadingBookShelfEvent()

	data class MakeToast(
		val stringId: Int,
	) : ReadingBookShelfEvent()

}

sealed interface ReadingBookShelfItem {

	fun getCategoryId(): Long {
		return when (this) {
			is Header -> HEADER_ITEM_STABLE_ID
			is Item -> bookShelfId
		}
	}

	data class Header(val totalItemCount: Int) : ReadingBookShelfItem
	data class Item(
		val bookShelfId: Long,
		val book: Book,
		val pages: Int,
		val state: BookShelfState,
		val star: StarRating? = null,
		val lastUpdatedAt: Date,
		val isSwiped: Boolean = false,
	) : ReadingBookShelfItem

	private companion object {
		private const val HEADER_ITEM_STABLE_ID = -1L
	}
}