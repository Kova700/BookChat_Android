package com.kova700.bookchat.feature.search.dialog

import com.kova700.bookchat.core.data.bookshelf.external.model.BookShelfState
import com.kova700.bookchat.core.data.search.book.external.model.Book
import com.kova700.bookchat.core.data.search.channel.external.model.SearchFilter
import com.kova700.bookchat.feature.search.model.SearchPurpose
import com.kova700.bookchat.feature.search.model.SearchTarget

data class SearchDialogUiState(
	val uiState: SearchDialogState,
	val book: Book,
	val starRating: Float,
	val bookshelfState: BookShelfState?,
) {
	val isSuccess: Boolean
		get() = uiState is SearchDialogState.Success

	val isLoading: Boolean
		get() = uiState is SearchDialogState.Loading

	val isInitError: Boolean
		get() = uiState is SearchDialogState.InitError

	val isNotInBookShelf: Boolean
		get() = uiState is SearchDialogState.Success
						&& uiState.bookShelfState == null

	val isAlreadyInBookShelf: Boolean
		get() = uiState is SearchDialogState.Success
						&& uiState.bookShelfState != null

	val isAlreadyInWishBookShelf: Boolean
		get() = uiState is SearchDialogState.Success
						&& uiState.bookShelfState == BookShelfState.WISH

	sealed class SearchDialogState {
		data class Success(val bookShelfState: BookShelfState?) : SearchDialogState()
		data object Loading : SearchDialogState()
		data object InitError : SearchDialogState()
	}

	companion object {
		val DEFAULT = SearchDialogUiState(
			uiState = SearchDialogState.Success(null),
			book = Book.DEFAULT,
			starRating = 0F,
			bookshelfState = null,
		)
	}
}

sealed class SearchTapDialogEvent {
	data object MoveToStarSetDialog : SearchTapDialogEvent()
	data class ShowSnackBar(
		val stringId: Int,
	) : SearchTapDialogEvent()

	data class MoveToChannelSearchWithSelectedBook(
		val searchKeyword: String,
		val searchTarget: SearchTarget,
		val searchPurpose: SearchPurpose,
		val searchFilter: SearchFilter,
	) : SearchTapDialogEvent()
}