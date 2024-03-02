package com.example.bookchat.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import androidx.paging.map
import com.example.bookchat.data.paging.ReadingBookTapPagingSource
import com.example.bookchat.data.repository.ChattingRepositoryFacade
import com.example.bookchat.domain.repository.BookShelfRepository
import com.example.bookchat.domain.repository.ClientRepository
import com.example.bookchat.ui.viewmodel.contract.HomeUiState
import com.example.bookchat.ui.viewmodel.contract.HomeUiState.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

//TODO : 도서, 채팅 Room에서 가져오는 로직으로 수정
@HiltViewModel
class HomeViewModel @Inject constructor(
	private val bookShelfRepository: BookShelfRepository,
	private val clientRepository: ClientRepository,
	private val chattingRepositoryFacade: ChattingRepositoryFacade
) : ViewModel() {

	private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.DEFAULT)
	val uiState = _uiState.asStateFlow()

	init {
		getClientInfo()
		observeChannels()
		getChannels()
	}

	//PagingSource로 가져오는게 아닌,
	// API로 1회 호출해서 가져오게 수정
	val readingBookResult by lazy {
		Pager(
			config = PagingConfig(
				pageSize = 10,
				enablePlaceholders = false
			),
			pagingSourceFactory = {
				ReadingBookTapPagingSource(
					bookShelfRepository = bookShelfRepository
				)
			}
		).flow
			.map { pagingData ->
				pagingData.map { pair ->
					pair.first.getBookShelfDataItem()
				}
			}.cachedIn(viewModelScope)
	}

	private fun getReadingBooks() = viewModelScope.launch {
		updateState { copy(bookUiState = UiState.LOADING) }
//		runCatching { bookShelfRepository.getBookShelfBooks() }
//			.onSuccess {
//				updateState { copy(bookUiState = UiState.SUCCESS) }
//			}
//			.onFailure {
//				handleError(it)
//				updateState { copy(bookUiState = UiState.ERROR) }
//			}
	}

	private fun observeChannels() = viewModelScope.launch {
		chattingRepositoryFacade.getChannelsFlow().collect { channels ->
			updateState { copy(channels = channels.take(3)) }
		}
	}

	private fun getChannels() = viewModelScope.launch {
		updateState { copy(channelUiState = UiState.LOADING) }
		runCatching { chattingRepositoryFacade.getChannels() }
			.onSuccess {
				updateState { copy(channelUiState = UiState.SUCCESS) }
			}
			.onFailure {
				handleError(it)
				updateState { copy(channelUiState = UiState.ERROR) }
			}
	}

	private fun getClientInfo() = viewModelScope.launch {
		runCatching { clientRepository.getClientProfile() }
			.onSuccess { user -> updateState { copy(client = user) } }
	}

	private fun handleError(throwable: Throwable) {

	}

	private inline fun updateState(block: HomeUiState.() -> HomeUiState) {
		_uiState.update {
			_uiState.value.block()
		}
	}
}