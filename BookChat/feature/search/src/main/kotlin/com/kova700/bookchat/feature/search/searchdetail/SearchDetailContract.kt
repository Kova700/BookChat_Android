package com.kova700.bookchat.feature.search.searchdetail

import com.kova700.bookchat.core.data.search.book.external.model.Book
import com.kova700.bookchat.core.data.search.channel.external.model.SearchFilter
import com.kova700.bookchat.feature.search.model.SearchPurpose
import com.kova700.bookchat.feature.search.model.SearchResultItem
import com.kova700.bookchat.feature.search.model.SearchTarget

data class SearchDetailUiState(
	val uiState: UiState,
	val searchKeyword: String,
	val searchPurpose: SearchPurpose,
	val searchTarget: SearchTarget?,
	val searchFilter: SearchFilter,
	val searchItems: List<SearchResultItem>,
) {

	enum class UiState {
		SUCCESS,
		LOADING,
		ERROR,
		EMPTY,
	}

	companion object {
		val DEFAULT = SearchDetailUiState(
			uiState = UiState.LOADING,
			searchKeyword = "",
			searchPurpose = SearchPurpose.SEARCH_BOTH,
			searchTarget = null,
			searchFilter = SearchFilter.BOOK_TITLE,
			searchItems = emptyList()
		)
	}
}

sealed class SearchDetailEvent {

	data object MoveToBack : SearchDetailEvent()

	data class MoveToSearchBookDialog(
		val book: Book,
	) : SearchDetailEvent()

	data class MoveToMakeChannelSelectBookDialog(
		val book: Book,
	) : SearchDetailEvent()

	data class MoveToChannelInfo(
		val channelId: Long,
	) : SearchDetailEvent()

	data class ShowSnackBar(
		val stringId: Int,
	) : SearchDetailEvent()
}
