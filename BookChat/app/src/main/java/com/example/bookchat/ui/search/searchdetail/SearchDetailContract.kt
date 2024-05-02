package com.example.bookchat.ui.search.searchdetail

import com.example.bookchat.domain.model.Book
import com.example.bookchat.domain.model.SearchFilter
import com.example.bookchat.domain.model.SearchPurpose
import com.example.bookchat.ui.search.model.SearchResultItem
import com.example.bookchat.ui.search.model.SearchTarget

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

	object MoveToBack : SearchDetailEvent()

	data class MoveToSearchBookDialog(
		val book: Book
	) : SearchDetailEvent()

	data class MoveToMakeChannelSelectBookDialog(
		val book: Book
	) : SearchDetailEvent()

	data class MoveToChannelInfo(
		val channelId: Long
	) : SearchDetailEvent()
}
