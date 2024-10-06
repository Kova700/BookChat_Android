package com.kova700.bookchat.feature.search.searchdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kova700.bookchat.core.data.search.book.external.BookSearchRepository
import com.kova700.bookchat.core.data.search.channel.external.ChannelSearchRepository
import com.kova700.bookchat.core.data.search.channel.external.model.SearchFilter
import com.kova700.bookchat.core.design_system.R
import com.kova700.bookchat.feature.search.SearchFragment.Companion.EXTRA_SEARCH_FILTER
import com.kova700.bookchat.feature.search.SearchFragment.Companion.EXTRA_SEARCH_KEYWORD
import com.kova700.bookchat.feature.search.SearchFragment.Companion.EXTRA_SEARCH_PURPOSE
import com.kova700.bookchat.feature.search.SearchFragment.Companion.EXTRA_SEARCH_TARGET
import com.kova700.bookchat.feature.search.mapper.toBook
import com.kova700.bookchat.feature.search.model.SearchPurpose
import com.kova700.bookchat.feature.search.model.SearchPurpose.MAKE_CHANNEL
import com.kova700.bookchat.feature.search.model.SearchPurpose.SEARCH_BOTH
import com.kova700.bookchat.feature.search.model.SearchPurpose.SEARCH_CHANNEL
import com.kova700.bookchat.feature.search.model.SearchResultItem
import com.kova700.bookchat.feature.search.model.SearchTarget
import com.kova700.bookchat.feature.search.searchdetail.SearchDetailUiState.UiState
import com.kova700.bookchat.feature.search.searchdetail.mapper.toBookSearchResultDetailItem
import com.kova700.bookchat.feature.search.searchdetail.mapper.toChannelSearchResultDetailItem
import com.kova700.bookchat.util.book.BookImgSizeManager
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
class SearchDetailViewModel @Inject constructor(
	private val savedStateHandle: SavedStateHandle,
	private val bookSearchRepository: BookSearchRepository,
	private val channelSearchRepository: ChannelSearchRepository,
	private val bookImgSizeManager: BookImgSizeManager,
) : ViewModel() {
	private val searchKeyword = savedStateHandle.get<String>(EXTRA_SEARCH_KEYWORD)!!
	private val searchTarget = savedStateHandle.get<SearchTarget>(EXTRA_SEARCH_TARGET)!!
	private val searchPurpose = savedStateHandle.get<SearchPurpose>(EXTRA_SEARCH_PURPOSE)!!
	private val searchFilter = savedStateHandle.get<SearchFilter>(EXTRA_SEARCH_FILTER)!!

	private val _eventFlow = MutableSharedFlow<SearchDetailEvent>()
	val eventFlow get() = _eventFlow.asSharedFlow()

	private val _uiState = MutableStateFlow<SearchDetailUiState>(SearchDetailUiState.DEFAULT)
	val uiState get() = _uiState.asStateFlow()

	init {
		initUiState()
	}

	private fun initUiState() {
		updateState {
			copy(
				searchKeyword = this@SearchDetailViewModel.searchKeyword,
				searchTarget = this@SearchDetailViewModel.searchTarget,
				searchPurpose = this@SearchDetailViewModel.searchPurpose,
				searchFilter = this@SearchDetailViewModel.searchFilter
			)
		}
		getInitSearchItems()
		observeSearchItems()
	}

	private fun observeSearchItems() = viewModelScope.launch {
		when (searchTarget) {
			SearchTarget.BOOK -> {
				combine(
					bookSearchRepository.getBooksFLow(),
					uiState.map { it.uiState }.distinctUntilChanged(),
				) { books, uiState ->
					books.toBookSearchResultDetailItem(
						bookImgSizeManager = bookImgSizeManager,
						uiState = uiState
					)
				}.collect { items -> updateState { copy(searchItems = items) } }
			}

			SearchTarget.CHANNEL -> {
				combine(
					channelSearchRepository.getChannelsFLow(),
					uiState.map { it.uiState }.distinctUntilChanged(),
				) { channels, uiState -> channels.toChannelSearchResultDetailItem(uiState) }
					.collect { items -> updateState { copy(searchItems = items) } }
			}
		}
	}

	private fun getInitSearchItems() = viewModelScope.launch {
		if (uiState.value.isLoading) return@launch
		updateState { copy(uiState = UiState.INIT_LOADING) }
		when (searchTarget) {
			SearchTarget.BOOK -> searchBooks()
			SearchTarget.CHANNEL -> searchChannels()
		}.onFailure {
			updateState { copy(uiState = UiState.INIT_ERROR) }
			startEvent(SearchDetailEvent.ShowSnackBar(R.string.error_else))
		}
	}

	private fun getSearchItems() = viewModelScope.launch {
		if (uiState.value.isLoading) return@launch
		updateState { copy(uiState = UiState.PAGING_LOADING) }
		when (searchTarget) {
			SearchTarget.BOOK -> searchBooks()
			SearchTarget.CHANNEL -> searchChannels()
		}.onFailure {
			updateState { copy(uiState = UiState.PAGING_ERROR) }
			startEvent(SearchDetailEvent.ShowSnackBar(R.string.error_else))
		}
	}

	private suspend fun searchBooks() =
		runCatching { bookSearchRepository.search(searchKeyword) }
			.onSuccess { updateState { copy(uiState = UiState.SUCCESS) } }

	private suspend fun searchChannels() =
		runCatching {
			channelSearchRepository.search(
				keyword = searchKeyword,
				searchFilter = uiState.value.searchFilter,
			)
		}.onSuccess { updateState { copy(uiState = UiState.SUCCESS) } }

	fun loadNextData(lastVisibleItemPosition: Int) {
		if (uiState.value.searchItems.size - 1 > lastVisibleItemPosition
			|| uiState.value.isLoading
		) return
		getSearchItems()
	}

	fun onBookItemClick(bookItem: SearchResultItem.BookItem) {
		when (uiState.value.searchPurpose) {
			SEARCH_BOTH -> startEvent(SearchDetailEvent.MoveToSearchBookDialog(bookItem.toBook()))
			MAKE_CHANNEL -> startEvent(SearchDetailEvent.MoveToMakeChannelSelectBookDialog(bookItem.toBook()))
			SEARCH_CHANNEL -> Unit
		}
	}

	fun onChannelItemClick(channelId: Long) {
		startEvent(SearchDetailEvent.MoveToChannelInfo(channelId))
	}

	fun onClickBackBtn() {
		startEvent(SearchDetailEvent.MoveToBack)
	}

	fun onInitRetryBtnClick() {
		getInitSearchItems()
	}

	fun onPagingRetryBtnClick() {
		getSearchItems()
	}

	private fun startEvent(event: SearchDetailEvent) = viewModelScope.launch {
		_eventFlow.emit(event)
	}

	private inline fun updateState(block: SearchDetailUiState.() -> SearchDetailUiState) {
		_uiState.update { _uiState.value.block() }
	}

}