package com.example.bookchat.ui.search.dialog

import com.example.bookchat.domain.model.Book
import com.example.bookchat.domain.model.BookShelfState
import com.example.bookchat.domain.model.SearchFilter
import com.example.bookchat.domain.model.SearchPurpose
import com.example.bookchat.ui.search.model.SearchTarget

data class SearchDialogUiState(
	val uiState: SearchDialogState,
	val book: Book,
	val starRating: Float,
) {

	val isAlreadyInWishBookShelf: Boolean
		get() = uiState is SearchDialogState.AlreadyInBookShelf
						&& uiState.bookShelfState == BookShelfState.WISH

	sealed class SearchDialogState {
		data object Default : SearchDialogState()
		data object Loading : SearchDialogState()
		data class AlreadyInBookShelf(val bookShelfState: BookShelfState) : SearchDialogState()
	}

	companion object {
		val DEFAULT = SearchDialogUiState(
			uiState = SearchDialogState.Loading,
			book = Book.DEFAULT,
			starRating = 0F,
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