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
	val isLoading: Boolean
		get() = isPagingLoading || isInitLoading

	val isPagingLoading: Boolean
		get() = uiState == UiState.PAGING_LOADING

	val isInitLoading: Boolean
		get() = uiState == UiState.INIT_LOADING

	val isInitError: Boolean
		get() = uiState == UiState.INIT_ERROR

	val isEmpty: Boolean
		get() = searchItems.isEmpty()
						&& isInitLoading.not()
						&& isInitError.not()

	val isNotEmpty: Boolean
		get() = searchItems.isNotEmpty()
						&& isInitLoading.not()
						&& isInitError.not()

	enum class UiState {
		SUCCESS,
		INIT_LOADING,
		INIT_ERROR,
		PAGING_LOADING,
		PAGING_ERROR,
	}

	companion object {
		val DEFAULT = SearchDetailUiState(
			uiState = UiState.SUCCESS,
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
