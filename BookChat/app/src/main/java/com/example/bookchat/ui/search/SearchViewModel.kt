package com.example.bookchat.ui.search

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookchat.R
import com.example.bookchat.data.network.model.response.NetworkIsNotConnectedException
import com.example.bookchat.domain.model.Book
import com.example.bookchat.domain.model.Channel
import com.example.bookchat.domain.model.SearchPurpose
import com.example.bookchat.domain.repository.BookSearchRepository
import com.example.bookchat.domain.repository.ChannelSearchRepository
import com.example.bookchat.domain.repository.SearchHistoryRepository
import com.example.bookchat.ui.search.SearchFragment.Companion.EXTRA_SEARCH_PURPOSE
import com.example.bookchat.ui.search.SearchUiState.SearchResultState
import com.example.bookchat.ui.search.SearchUiState.SearchTapState
import com.example.bookchat.ui.search.mapper.toBook
import com.example.bookchat.ui.search.mapper.toBookItem
import com.example.bookchat.ui.search.mapper.toChannelItem
import com.example.bookchat.ui.search.model.SearchResultItem
import com.example.bookchat.ui.search.model.SearchTarget
import com.example.bookchat.utils.BookImgSizeManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
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
	private val bookImgSizeManager: BookImgSizeManager
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
				) { channels, books -> groupItems(channels = channels, books = books) }
					.collect { items -> updateState { copy(searchResults = items) } }
			}

			SearchPurpose.MAKE_CHANNEL -> {
				bookSearchRepository.getBooksFLow(true).map { groupItems(books = it) }
					.collect { items -> updateState { copy(searchResults = items) } }
			}

			SearchPurpose.SEARCH_CHANNEL -> {
				channelSearchRepository.getChannelsFLow(true).map { groupItems(channels = it) }
					.collect { items -> updateState { copy(searchResults = items) } }
			}
		}
	}

	private fun observeSearchHistory() = viewModelScope.launch {
		searchHistoryRepository.getSearchHistoryFlow().collect {
			updateState { copy(searchHistory = it) }
		}
	}

	private fun groupItems(
		books: List<Book>? = null,
		channels: List<Channel>? = null
	): List<SearchResultItem> {
		val groupedItems = mutableListOf<SearchResultItem>()
		if (books.isNullOrEmpty() && channels.isNullOrEmpty()) return groupedItems

		fun addBookItems() {
			groupedItems.add(SearchResultItem.BookHeader)
			if (books.isNullOrEmpty()) {
				groupedItems.add(SearchResultItem.BookEmpty)
			} else {
				val defaultSize = bookImgSizeManager.flexBoxBookSpanSize * 2
				val exposureItemCount = if (books.size > defaultSize) defaultSize else books.size
				groupedItems.addAll(books.take(exposureItemCount).map { it.toBookItem() })

				val dummyItemCount = bookImgSizeManager.getFlexBoxDummyItemCount(exposureItemCount)
				(0 until dummyItemCount).forEach { i -> groupedItems.add(SearchResultItem.BookDummy(i)) }
			}
		}

		fun addChannelItems() {
			groupedItems.add(SearchResultItem.ChannelHeader)
			if (channels.isNullOrEmpty()) {
				groupedItems.add(SearchResultItem.ChannelEmpty)
			} else {
				val exposureItemCount = if (channels.size > 3) 3 else channels.size
				groupedItems.addAll(channels.take(exposureItemCount).map { it.toChannelItem() })
			}
		}

		when (uiState.value.searchPurpose) {
			SearchPurpose.SEARCH_BOTH -> {
				addBookItems()
				addChannelItems()
			}

			SearchPurpose.MAKE_CHANNEL -> {
				addBookItems()
			}

			SearchPurpose.SEARCH_CHANNEL -> {
				addChannelItems()
			}
		}
		return groupedItems
	}

	//TODO : 현재 도서명으로만 검색되고 있는거 같음 , 채팅방 명으로 검색안되고 있음
	private fun search(keyword: String) = viewModelScope.launch {
		updateState {
			copy(
				searchKeyword = keyword,
				searchTapState = SearchTapState.Result,
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
			val books = bookSearchRepository.search(
				keyword = searchKeyword.trim(),
				loadSize = bookImgSizeManager.flexBoxBookSpanSize * 6
			)
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
		runCatching {
			bookSearchRepository.search(
				keyword = searchKeyword.trim(),
				loadSize = bookImgSizeManager.flexBoxBookSpanSize * 6
			)
		}
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

	private suspend fun searchSuccessCallBack(isEmpty: Boolean) {
		delay(SKELETON_DURATION)
		if (isEmpty) updateState { copy(searchResultState = SearchResultState.Empty) }
		else updateState { copy(searchResultState = SearchResultState.Success) }
	}

	private fun clearSearchBar() {
		updateState {
			copy(
				searchKeyword = "",
				searchTapState = SearchTapState.Default
			)
		}
	}

	fun onSearchBarTextChange(text: String) {
		if (uiState.value.searchKeyword == text) return

		updateState {
			copy(
				searchKeyword = text,
				searchTapState = SearchTapState.Searching
			)
		}
	}

	fun onClickSearchBar() {
		updateState { copy(searchTapState = SearchTapState.History) }
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
		searchHistoryRepository.clearHistory()
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
			SearchPurpose.SEARCH_BOTH -> startEvent(SearchEvent.MoveToSearchBookDialog(bookItem.toBook()))
			SearchPurpose.MAKE_CHANNEL -> startEvent(
				SearchEvent.MoveToMakeChannelSelectBookDialog(bookItem.toBook())
			)

			SearchPurpose.SEARCH_CHANNEL -> Unit
		}
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
				startEvent(SearchEvent.MakeToast(R.string.error_network))

			else -> startEvent(SearchEvent.MakeToast(R.string.error_else))
		}
	}

	companion object {
		private const val SKELETON_DURATION = 700L
	}
}