package com.kova700.bookchat.feature.search

import com.kova700.bookchat.core.data.search.book.external.model.Book
import com.kova700.bookchat.core.data.search.channel.external.model.SearchFilter
import com.kova700.bookchat.feature.search.model.SearchPurpose
import com.kova700.bookchat.feature.search.model.SearchResultItem
import com.kova700.bookchat.feature.search.model.SearchTarget
import com.kova700.bookchat.feature.search.R as searchR

data class SearchUiState(
	val searchTapState: SearchTapState,
	val searchPurpose: SearchPurpose,
	val searchFilter: SearchFilter,
	val searchKeyword: String,
	val searchResultState: SearchResultState,
	val searchResults: List<SearchResultItem>,
	val searchHistory: List<String>,
) {
	val isBothEmptyResult
		get() = (searchTapState is SearchTapState.Result)
						&& (searchResultState == SearchResultState.Empty)

	val isLoading
		get() = (searchTapState is SearchTapState.Result)
						&& (searchResultState == SearchResultState.Loading)

	val isErrorOrSuccessResult
		get() = (searchTapState is SearchTapState.Result)
						&& ((searchResultState == SearchResultState.Success)
						|| (searchResultState == SearchResultState.Error))

	sealed class SearchTapState(open val fragmentId: Int) {
		data class Default(
			override val fragmentId: Int = searchR.id.searchTapDefaultFragment,
		) : SearchTapState(fragmentId)

		data class History(
			override val fragmentId: Int = searchR.id.searchTapHistoryFragment,
		) : SearchTapState(fragmentId)

		data class Searching(
			override val fragmentId: Int = searchR.id.searchTapSearchingFragment,
		) : SearchTapState(fragmentId)

		data class Result(
			override val fragmentId: Int = searchR.id.searchTapResultFragment,
		) : SearchTapState(fragmentId)

		val isDefault get() = this is Default
		val isDefaultOrHistory get() = this is Default || this is History
	}

	sealed class SearchResultState {
		data object Loading : SearchResultState()
		data object Empty : SearchResultState()
		data object Error : SearchResultState()
		data object Success : SearchResultState()
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
	data object ShowSearchFilterSelectDialog : SearchEvent()

	data class MoveToDetail(
		val searchKeyword: String,
		val searchTarget: SearchTarget,
		val searchPurpose: SearchPurpose,
		val searchFilter: SearchFilter,
	) : SearchEvent()

	data class ShowSearchBookDialog(
		val book: Book,
	) : SearchEvent()

	data class ShowMakeChannelSelectBookDialog(
		val book: Book,
	) : SearchEvent()

	data class MoveToChannelInfo(
		val channelId: Long,
	) : SearchEvent()

	data object MoveToMakeChannel : SearchEvent()

	data class ShowSearchFilterChangeSnackBar(
		val stringId: Int,
	) : SearchEvent()

	data class ShowSnackBar(
		val stringId: Int,
	) : SearchEvent()

}
