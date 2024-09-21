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