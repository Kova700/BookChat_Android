package com.example.bookchat.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookchat.data.repository.ChattingRepositoryFacade
import com.example.bookchat.domain.model.BookShelfState
import com.example.bookchat.domain.repository.BookShelfRepository
import com.example.bookchat.domain.repository.ClientRepository
import com.example.bookchat.ui.bookshelf.mapper.toBookShelfListItem
import com.example.bookchat.ui.home.HomeUiState.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
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

	private val _eventFlow = MutableSharedFlow<HomeUiEvent>()
	val eventFlow = _eventFlow.asSharedFlow()

	private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.DEFAULT)
	val uiState = _uiState.asStateFlow()

	init {
		getClientInfo()
		getReadingBookShelfItems()
		observeChannels()
		getChannels()
		observeReadingBookShelfItems()
	}

	private fun observeReadingBookShelfItems() = viewModelScope.launch {
		bookShelfRepository.getBookShelfFlow(BookShelfState.READING).collect { bookShelfItems ->
			updateState { copy(readingBookShelfBooks = bookShelfItems.take(3).toBookShelfListItem()) }
		}
	}

	private fun getReadingBookShelfItems() = viewModelScope.launch {
		updateState { copy(bookUiState = UiState.LOADING) }
		runCatching { bookShelfRepository.getBookShelfItems(BookShelfState.READING) }
			.onSuccess {
				updateState { copy(bookUiState = UiState.SUCCESS) }
			}
			.onFailure {
				handleError(it)
				updateState { copy(bookUiState = UiState.ERROR) }
			}
	}

	private fun observeChannels() = viewModelScope.launch {
		chattingRepositoryFacade.getChannelsFlow().collect { channels ->
			updateState { copy(channels = channels.take(3)) }
		}
	}

	private fun getChannels() = viewModelScope.launch {
		updateState { copy(channelUiState = UiState.LOADING) }
		runCatching { chattingRepositoryFacade.getChannels() }
			.onSuccess { updateState { copy(channelUiState = UiState.SUCCESS) } }
			.onFailure {
				handleError(it)
			}
	}

	private fun getClientInfo() = viewModelScope.launch {
		runCatching { clientRepository.getClientProfile() }
			.onSuccess { user -> updateState { copy(client = user) } }
	}

	fun onBookItemClick(bookShelfListItemId: Long) {
		startEvent(HomeUiEvent.MoveToReadingBookShelf(bookShelfListItemId))
	}

	fun onChannelItemClick(channelId: Long) {
		startEvent(HomeUiEvent.MoveToChannel(channelId))
	}

	fun startEvent(event: HomeUiEvent) = viewModelScope.launch {
		_eventFlow.emit(event)
	}

	private fun handleError(throwable: Throwable) {
		updateState { copy(channelUiState = UiState.ERROR) }
	}

	private inline fun updateState(block: HomeUiState.() -> HomeUiState) {
		_uiState.update { _uiState.value.block() }
	}
}