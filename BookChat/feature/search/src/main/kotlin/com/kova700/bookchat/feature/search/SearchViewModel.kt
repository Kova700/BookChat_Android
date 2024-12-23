package com.kova700.bookchat.feature.search

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kova700.bookchat.core.data.search.book.external.BookSearchRepository
import com.kova700.bookchat.core.data.search.channel.external.ChannelSearchRepository
import com.kova700.bookchat.core.data.search.channel.external.model.SearchFilter
import com.kova700.bookchat.core.design_system.R
import com.kova700.bookchat.feature.search.SearchFragment.Companion.EXTRA_SEARCH_PURPOSE
import com.kova700.bookchat.feature.search.SearchUiState.SearchResultUiState
import com.kova700.bookchat.feature.search.SearchUiState.SearchTapState
import com.kova700.bookchat.feature.search.mapper.groupItems
import com.kova700.bookchat.feature.search.mapper.toBook
import com.kova700.bookchat.feature.search.model.SearchPurpose
import com.kova700.bookchat.feature.search.model.SearchResultItem
import com.kova700.bookchat.feature.search.model.SearchTarget
import com.kova700.bookchat.util.book.BookImgSizeManager
import com.kova700.core.data.searchhistory.external.SearchHistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
	private val savedStateHandle: SavedStateHandle,
	private val bookSearchRepository: BookSearchRepository,
	private val channelSearchRepository: ChannelSearchRepository,
	private val searchHistoryRepository: SearchHistoryRepository,
	private val bookImgSizeManager: BookImgSizeManager,
) : ViewModel() {

	private val searchPurpose =
		savedStateHandle.get<SearchPurpose>(EXTRA_SEARCH_PURPOSE) ?: SearchPurpose.SEARCH_BOTH

	private val _eventFlow = MutableSharedFlow<SearchEvent>()
	val eventFlow get() = _eventFlow.asSharedFlow()

	private val _uiState = MutableStateFlow<SearchUiState>(SearchUiState.DEFAULT)
	val uiState get() = _uiState.asStateFlow()

	init {
		initUiState()
		observeSearchHistory()
		observeSearchItems()
	}

	private fun initUiState() {
		updateState { copy(searchPurpose = this@SearchViewModel.searchPurpose) }
	}

	private fun observeSearchItems() = viewModelScope.launch {
		when (searchPurpose) {

			SearchPurpose.SEARCH_BOTH -> {
				combine(
					channelSearchRepository.getChannelsFLow(true),
					bookSearchRepository.getBooksFLow(true),
					uiState.map { it.bookSearchResultUiState }.distinctUntilChanged(),
					uiState.map { it.channelSearchResultUiState }.distinctUntilChanged(),
				) { channels, books, bookUiState, channelUiState ->
					groupItems(
						channels = channels,
						books = books,
						searchPurpose = searchPurpose,
						bookImgSizeManager = bookImgSizeManager,
						bookSearchResultUiState = bookUiState,
						channelSearchResultUiState = channelUiState,
					)
				}.collect { items -> updateState { copy(searchResults = items) } }
			}

			SearchPurpose.MAKE_CHANNEL -> {
				combine(
					bookSearchRepository.getBooksFLow(true),
					uiState.map { it.bookSearchResultUiState }.distinctUntilChanged(),
					uiState.map { it.channelSearchResultUiState }.distinctUntilChanged(),
				) { books, bookUiState, channelUiState ->
					groupItems(
						books = books,
						searchPurpose = searchPurpose,
						bookImgSizeManager = bookImgSizeManager,
						bookSearchResultUiState = bookUiState,
						channelSearchResultUiState = channelUiState,
					)
				}.collect { items -> updateState { copy(searchResults = items) } }
			}

			SearchPurpose.SEARCH_CHANNEL -> {
				combine(
					channelSearchRepository.getChannelsFLow(true),
					uiState.map { it.bookSearchResultUiState }.distinctUntilChanged(),
					uiState.map { it.channelSearchResultUiState }.distinctUntilChanged(),
				) { channels, bookUiState, channelUiState ->
					groupItems(
						channels = channels,
						searchPurpose = searchPurpose,
						bookImgSizeManager = bookImgSizeManager,
						bookSearchResultUiState = bookUiState,
						channelSearchResultUiState = channelUiState,
					)
				}.collect { items -> updateState { copy(searchResults = items) } }
			}
		}
	}

	private fun observeSearchHistory() = viewModelScope.launch {
		searchHistoryRepository.getSearchHistoryFlow().collect {
			updateState { copy(searchHistory = it) }
		}
	}

	private fun search(keyword: String) = viewModelScope.launch {
		updateState {
			copy(
				searchKeyword = keyword,
				searchTapState = SearchTapState.Result(),
			)
		}
		when (uiState.value.searchPurpose) {
			SearchPurpose.SEARCH_BOTH -> {
				searchBooks(keyword)
				searchChannels(keyword)
			}

			SearchPurpose.MAKE_CHANNEL -> searchBooks(keyword)
			SearchPurpose.SEARCH_CHANNEL -> searchChannels(keyword)
		}
	}

	private fun searchBooks(searchKeyword: String) = viewModelScope.launch {
		if (uiState.value.isBookSearchLoading) return@launch
		updateState { copy(bookSearchResultUiState = SearchResultUiState.INIT_LOADING) }
		runCatching { bookSearchRepository.search(searchKeyword.trim()) }
			.onSuccess { updateState { copy(bookSearchResultUiState = SearchResultUiState.SUCCESS) } }
			.onFailure {
				updateState { copy(bookSearchResultUiState = SearchResultUiState.INIT_ERROR) }
				startEvent(SearchEvent.ShowSnackBar(R.string.error_else))
			}
	}

	private fun searchChannels(searchKeyword: String) = viewModelScope.launch {
		if (uiState.value.isChannelSearchLoading) return@launch
		updateState { copy(channelSearchResultUiState = SearchResultUiState.INIT_LOADING) }
		runCatching {
			channelSearchRepository.search(
				keyword = searchKeyword.trim(),
				searchFilter = uiState.value.searchFilter,
				initFlag = true
			)
		}.onSuccess { updateState { copy(channelSearchResultUiState = SearchResultUiState.SUCCESS) } }
			.onFailure {
				updateState { copy(channelSearchResultUiState = SearchResultUiState.INIT_ERROR) }
				startEvent(SearchEvent.ShowSnackBar(R.string.error_else))
			}
	}

	private fun clearSearchBar() {
		updateState {
			copy(
				searchKeyword = "",
				searchTapState = SearchTapState.Default()
			)
		}
	}

	fun onSearchBarTextChange(text: String) {
		if (uiState.value.searchKeyword == text) return

		updateState {
			copy(
				searchKeyword = text,
				searchTapState = SearchTapState.Searching()
			)
		}
	}

	fun onClickSearchBar() {
		updateState { copy(searchTapState = SearchTapState.History()) }
	}

	fun onClickSearchBtn() = viewModelScope.launch {
		val keyword = uiState.value.searchKeyword.trim()
		if (keyword.isBlank()) {
			startEvent(SearchEvent.ShowSnackBar(R.string.search_book_keyword_empty))
			return@launch
		}
		searchHistoryRepository.addHistory(keyword)
		search(keyword)
	}

	fun onClickBookRetryBtn() {
		searchBooks(uiState.value.searchKeyword)
	}

	fun onClickChannelRetryBtn() {
		searchChannels(uiState.value.searchKeyword)
	}

	fun onClickSearchHistory(position: Int) = viewModelScope.launch {
		val keyword = uiState.value.searchHistory[position]
		search(keyword)
		searchHistoryRepository.moveHistoryAtTheTop(position)
	}

	fun onClickSearchHistoryDeleteBtn(position: Int) = viewModelScope.launch {
		searchHistoryRepository.removeHistory(position)
	}

	fun onClickHistoryClearBtn() = viewModelScope.launch {
		searchHistoryRepository.clear()
	}

	fun onBookHeaderBtnClick() {
		startEvent(
			SearchEvent.MoveToDetail(
				searchKeyword = uiState.value.searchKeyword,
				searchTarget = SearchTarget.BOOK,
				searchPurpose = uiState.value.searchPurpose,
				searchFilter = uiState.value.searchFilter,
			)
		)
	}

	fun onChannelHeaderBtnClick() {
		startEvent(
			SearchEvent.MoveToDetail(
				searchKeyword = uiState.value.searchKeyword,
				searchTarget = SearchTarget.CHANNEL,
				searchPurpose = uiState.value.searchPurpose,
				searchFilter = uiState.value.searchFilter,
			)
		)
	}

	fun onBookItemClick(bookItem: SearchResultItem.BookItem) {
		when (uiState.value.searchPurpose) {
			SearchPurpose.SEARCH_BOTH -> startEvent(SearchEvent.ShowSearchBookDialog(bookItem.toBook()))
			SearchPurpose.MAKE_CHANNEL -> startEvent(
				SearchEvent.ShowMakeChannelSelectBookDialog(bookItem.toBook())
			)

			SearchPurpose.SEARCH_CHANNEL -> Unit
		}
	}

	fun onClickSearchFilter(selectedFilter: SearchFilter) {
		updateState { copy(searchFilter = selectedFilter) }
		val resId = when (selectedFilter) {
			SearchFilter.BOOK_TITLE -> R.string.book_title
			SearchFilter.BOOK_ISBN -> R.string.isbn
			SearchFilter.ROOM_NAME -> R.string.channel_title
			SearchFilter.ROOM_TAGS -> R.string.channel_tag
		}
		startEvent(SearchEvent.ShowSearchFilterChangeSnackBar(resId))
	}

	fun onClickSearchFilterBtn() {
		startEvent(SearchEvent.ShowSearchFilterSelectDialog)
	}

	fun onChannelItemClick(channelId: Long) {
		startEvent(SearchEvent.MoveToChannelInfo(channelId))
	}

	fun onClickBackBtn() {
		clearSearchBar()
	}

	fun onClickKeywordClearBtn() {
		clearSearchBar()
	}

	fun onClickMakeChannelBtn() {
		startEvent(SearchEvent.MoveToMakeChannel)
	}

	private fun startEvent(event: SearchEvent) = viewModelScope.launch {
		_eventFlow.emit(event)
	}

	private inline fun updateState(block: SearchUiState.() -> SearchUiState) {
		_uiState.update { _uiState.value.block() }
	}

}