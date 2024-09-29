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
import com.kova700.bookchat.feature.search.mapper.toBookSearchResultItem
import com.kova700.bookchat.feature.search.mapper.toChannelSearchResultItem
import com.kova700.bookchat.feature.search.model.SearchPurpose
import com.kova700.bookchat.feature.search.model.SearchResultItem
import com.kova700.bookchat.feature.search.model.SearchTarget
import com.kova700.bookchat.feature.search.searchdetail.SearchDetailUiState.UiState
import com.kova700.bookchat.util.book.BookImgSizeManager
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
					//이렇게 Empty 상태를 정의하는 방법 1
					//Uistate에 Item이 비어있고, State가 Success면 isEmpty로 처리하는 방법 2 (like : Bookshelf)
					//최초의 Search 함수 호출에 Result가 비어있다면 Empty로 처리하는 방법 3 (If문 만들어서 isFirst Or isInit로 분기문 작성)
					//     3번 방법은 검색처럼 데이터 가공이 없는 경우는 유효하나,
					//     유저가 데이터의 CRUD가 가능한 경우 검색을 안하고도 DataSet이 변경되는 경우가 생김으로 유효하지 않음
					//     고로 BookShelf와 같이 검색이 아닌 경우는 1번 혹은 2번이 유효함
					//     검색의 경우는 3번만으로도 충분함 (통일을 위해서 1번 혹은 2번으로 통일하는게 좋음 == 2번이 제일 깔끔하긴할듯
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
		updateState { copy(uiState = UiState.LOADING) }
		when (searchTarget) {
			SearchTarget.BOOK -> searchBooks()
			SearchTarget.CHANNEL -> searchChannels()
		}
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
			else -> startEvent(SearchDetailEvent.ShowSnackBar(R.string.error_else))
		}
	}

}