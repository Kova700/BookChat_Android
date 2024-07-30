package com.example.bookchat.ui.search.searchdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookchat.R
import com.example.bookchat.domain.model.SearchFilter
import com.example.bookchat.domain.model.SearchPurpose
import com.example.bookchat.domain.repository.BookSearchRepository
import com.example.bookchat.domain.repository.ChannelSearchRepository
import com.example.bookchat.ui.search.SearchFragment.Companion.EXTRA_SEARCH_FILTER
import com.example.bookchat.ui.search.SearchFragment.Companion.EXTRA_SEARCH_KEYWORD
import com.example.bookchat.ui.search.SearchFragment.Companion.EXTRA_SEARCH_PURPOSE
import com.example.bookchat.ui.search.SearchFragment.Companion.EXTRA_SEARCH_TARGET
import com.example.bookchat.ui.search.mapper.toBook
import com.example.bookchat.ui.search.mapper.toBookSearchResultItem
import com.example.bookchat.ui.search.mapper.toChannelSearchResultItem
import com.example.bookchat.ui.search.model.SearchResultItem
import com.example.bookchat.ui.search.model.SearchTarget
import com.example.bookchat.ui.search.searchdetail.SearchDetailUiState.UiState
import com.example.bookchat.utils.BookImgSizeManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
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
		observeSearchItems()
	}

	private fun initUiState() {
		updateState {
			copy(
				uiState = UiState.SUCCESS,
				searchKeyword = this@SearchDetailViewModel.searchKeyword,
				searchTarget = this@SearchDetailViewModel.searchTarget,
				searchPurpose = this@SearchDetailViewModel.searchPurpose,
				searchFilter = this@SearchDetailViewModel.searchFilter
			)
		}
	}

	private fun observeSearchItems() = viewModelScope.launch {
		when (searchTarget) {
			SearchTarget.BOOK -> {
				bookSearchRepository.getBooksFLow().map {
					it.toBookSearchResultItem(bookImgSizeManager.getFlexBoxDummyItemCount(it.size))
				}.collect { items ->
					if (items.isEmpty()) updateState { copy(uiState = UiState.EMPTY) }
					else updateState { copy(searchItems = items) }
				}
			}

			SearchTarget.CHANNEL -> {
				channelSearchRepository.getChannelsFLow().map { it.toChannelSearchResultItem() }
					.collect { items ->
						if (items.isEmpty()) updateState { copy(uiState = UiState.EMPTY) }
						else updateState { copy(searchItems = items) }
					}
			}
		}
	}

	private fun getSearchItems() = viewModelScope.launch {
		startEvent(SearchDetailEvent.ShowSnackBar(R.string.error_else))
//		updateState { copy(uiState = UiState.LOADING) }
//		when (searchTarget) {
//			SearchTarget.BOOK -> searchBooks()
//			SearchTarget.CHANNEL -> searchChannels()
//		}
	}

	private fun searchBooks() = viewModelScope.launch {
		runCatching { bookSearchRepository.search(searchKeyword) }
			.onSuccess { updateState { copy(uiState = UiState.SUCCESS) } }
			.onFailure { failHandler(it) }
	}

	private fun searchChannels() = viewModelScope.launch {
		runCatching {
			channelSearchRepository.search(
				keyword = searchKeyword,
				searchFilter = uiState.value.searchFilter,
			)
		}
			.onSuccess { updateState { copy(uiState = UiState.SUCCESS) } }
			.onFailure { failHandler(it) }
	}

	fun loadNextData(lastVisibleItemPosition: Int) {
		if (uiState.value.searchItems.size - 1 > lastVisibleItemPosition
			|| uiState.value.uiState == UiState.LOADING
		) return
		getSearchItems()
	}

	fun onBookItemClick(bookItem: SearchResultItem.BookItem) {
		when (uiState.value.searchPurpose) {
			SearchPurpose.SEARCH_BOTH -> startEvent(SearchDetailEvent.MoveToSearchBookDialog(bookItem.toBook()))
			SearchPurpose.MAKE_CHANNEL -> startEvent(
				SearchDetailEvent.MoveToMakeChannelSelectBookDialog(bookItem.toBook())
			)

			SearchPurpose.SEARCH_CHANNEL -> Unit
		}
	}

	fun onClickBackBtn() {
		startEvent(SearchDetailEvent.MoveToBack)
	}

	fun onChannelItemClick(channelId: Long) {
		startEvent(SearchDetailEvent.MoveToChannelInfo(channelId))
	}

	private fun startEvent(event: SearchDetailEvent) = viewModelScope.launch {
		_eventFlow.emit(event)
	}

	private inline fun updateState(block: SearchDetailUiState.() -> SearchDetailUiState) {
		_uiState.update { _uiState.value.block() }
	}

	private fun failHandler(exception: Throwable) {
		updateState { copy(uiState = UiState.ERROR) }
		when (exception) {
			else ->
				startEvent(SearchDetailEvent.ShowSnackBar(R.string.error_else))
		}
	}

}