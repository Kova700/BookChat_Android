package com.example.bookchat.ui.search

import com.example.bookchat.R
import com.example.bookchat.domain.model.Book
import com.example.bookchat.domain.model.SearchFilter
import com.example.bookchat.domain.model.SearchPurpose
import com.example.bookchat.ui.search.model.SearchResultItem
import com.example.bookchat.ui.search.model.SearchTarget

data class SearchUiState(
	val searchTapState: SearchTapState,
	val searchPurpose: SearchPurpose,
	val searchFilter: SearchFilter,
	val searchKeyword: String,
	val searchResultState: SearchResultState,
	val searchResults: List<SearchResultItem>,
	val searchHistory: List<String>,
) {

	sealed class SearchTapState(open val fragmentId: Int) {
		data class Default(
			override val fragmentId: Int = R.id.searchTapDefaultFragment,
		) : SearchTapState(fragmentId)

		data class History(
			override val fragmentId: Int = R.id.searchTapHistoryFragment,
		) : SearchTapState(fragmentId)

		data class Searching(
			override val fragmentId: Int = R.id.searchTapSearchingFragment,
		) : SearchTapState(fragmentId)

		data class Result(
			override val fragmentId: Int = R.id.searchTapResultFragment,
		) : SearchTapState(fragmentId)

		val isDefault get() = this is Default
		val isDefaultOrHistory get() = this is Default || this is History
	}

	sealed class SearchResultState {
		object Loading : SearchResultState()
		object Empty : SearchResultState()
		object Error : SearchResultState()
		object Success : SearchResultState()
	}

	companion object {
		val DEFAULT = SearchUiState(
			searchTapState = SearchTapState.Default(),
			searchPurpose = SearchPurpose.SEARCH_BOTH,
			searchFilter = SearchFilter.BOOK_TITLE,
			searchKeyword = "",
			searchResultState = SearchResultState.Loading,
			searchResults = emptyList(),
			searchHistory = emptyList()
		)
	}
}

sealed class SearchEvent {
	data class MoveToDetail(
		val searchKeyword: String,
		val searchTarget: SearchTarget,
		val searchPurpose: SearchPurpose,
		val searchFilter: SearchFilter,
	) : SearchEvent()

	data class MoveToSearchBookDialog(
		val book: Book,
	) : SearchEvent()

	data class MoveToMakeChannelSelectBookDialog(
		val book: Book,
	) : SearchEvent()

	data class MoveToChannelInfo(
		val channelId: Long,
	) : SearchEvent()

	data class MakeToast(
		val stringId: Int,
	) : SearchEvent()

}
