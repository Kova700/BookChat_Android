package com.example.bookchat.ui.search

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookchat.R
import com.example.bookchat.data.network.model.response.NetworkIsNotConnectedException
import com.example.bookchat.domain.model.SearchFilter
import com.example.bookchat.domain.model.SearchPurpose
import com.example.bookchat.domain.repository.BookSearchRepository
import com.example.bookchat.domain.repository.ChannelSearchRepository
import com.example.bookchat.domain.repository.SearchHistoryRepository
import com.example.bookchat.ui.search.SearchFragment.Companion.EXTRA_SEARCH_PURPOSE
import com.example.bookchat.ui.search.SearchUiState.SearchResultState
import com.example.bookchat.ui.search.SearchUiState.SearchTapState
import com.example.bookchat.ui.search.mapper.groupItems
import com.example.bookchat.ui.search.mapper.toBook
import com.example.bookchat.ui.search.model.SearchResultItem
import com.example.bookchat.ui.search.model.SearchTarget
import com.example.bookchat.utils.BookImgSizeManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

//TODO : 검색필터 채팅방 제목으로하고 자바 검색하면 타임아웃나옴 +
// 채팅방 검색 결과가 있는경우 시간이 너무 오래걸림 (없는 경우는 빨리 끝남)

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
					bookSearchRepository.getBooksFLow(true)
				) { channels, books ->
					groupItems(
						channels = channels,
						books = books,
						searchPurpose = searchPurpose,
						bookImgSizeManager = bookImgSizeManager
					)
				}.collect { items -> updateState { copy(searchResults = items) } }
			}

			SearchPurpose.MAKE_CHANNEL -> {
				bookSearchRepository.getBooksFLow(true).map {
					groupItems(
						books = it,
						searchPurpose = searchPurpose,
						bookImgSizeManager = bookImgSizeManager
					)
				}.collect { items -> updateState { copy(searchResults = items) } }
			}

			SearchPurpose.SEARCH_CHANNEL -> {
				channelSearchRepository.getChannelsFLow(true).map {
					groupItems(
						channels = it,
						searchPurpose = searchPurpose,
						bookImgSizeManager = bookImgSizeManager
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
				searchResultState = SearchResultState.Loading
			)
		}
		when (uiState.value.searchPurpose) {
			SearchPurpose.SEARCH_BOTH -> searchBooksAndChannels(keyword)
			SearchPurpose.MAKE_CHANNEL -> searchBooks(keyword)
			SearchPurpose.SEARCH_CHANNEL -> searchChannels(keyword)
		}
	}

	private fun searchBooksAndChannels(searchKeyword: String) = viewModelScope.launch {
		runCatching {
			val books = bookSearchRepository.search(searchKeyword.trim())
			val channels = channelSearchRepository.search(
				keyword = searchKeyword.trim(),
				searchFilter = uiState.value.searchFilter,
			)
			books.isEmpty() && channels.isEmpty()
		}
			.onSuccess { isEmpty -> searchSuccessCallBack(isEmpty) }
			.onFailure { failHandler(it) }
	}

	private fun searchBooks(searchKeyword: String) = viewModelScope.launch {
		runCatching { bookSearchRepository.search(searchKeyword.trim()) }
			.onSuccess { books -> searchSuccessCallBack(books.isEmpty()) }
			.onFailure { failHandler(it) }
	}

	private fun searchChannels(keyword: String) = viewModelScope.launch {
		runCatching {
			channelSearchRepository.search(
				keyword = keyword.trim(),
				searchFilter = uiState.value.searchFilter,
			)
		}
			.onSuccess { channels -> searchSuccessCallBack(channels.isEmpty()) }
			.onFailure { failHandler(it) }
	}

	private fun searchSuccessCallBack(isEmpty: Boolean) {
		if (isEmpty) updateState { copy(searchResultState = SearchResultState.Empty) }
		else updateState { copy(searchResultState = SearchResultState.Success) }
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
			startEvent(SearchEvent.MakeToast(R.string.search_book_keyword_empty))
			return@launch
		}
		searchHistoryRepository.addHistory(keyword)
		search(keyword)
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

	private fun failHandler(exception: Throwable) {
		updateState { copy(searchResultState = SearchResultState.Error) }
		when (exception) {
			is NetworkIsNotConnectedException ->
				startEvent(SearchEvent.MakeToast(R.string.error_network_not_connected))

			else -> startEvent(SearchEvent.MakeToast(R.string.error_else))
		}
	}

}