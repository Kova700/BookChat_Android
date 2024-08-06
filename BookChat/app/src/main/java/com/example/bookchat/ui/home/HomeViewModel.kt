package com.example.bookchat.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookchat.R
import com.example.bookchat.domain.model.BookShelfState
import com.example.bookchat.domain.repository.BookShelfRepository
import com.example.bookchat.domain.repository.ChannelRepository
import com.example.bookchat.domain.repository.ClientRepository
import com.example.bookchat.ui.home.HomeUiState.UiState
import com.example.bookchat.ui.home.mapper.groupItems
import com.example.bookchat.utils.BookImgSizeManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
	private val bookShelfRepository: BookShelfRepository,
	private val clientRepository: ClientRepository,
	private val channelRepository: ChannelRepository,
	private val bookImgSizeManager: BookImgSizeManager,
) : ViewModel() {

	private val _eventFlow = MutableSharedFlow<HomeUiEvent>()
	val eventFlow = _eventFlow.asSharedFlow()

	private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.DEFAULT)
	val uiState = _uiState.asStateFlow()

	init {
		observeClient()
		observeItems()
		getReadingBooks()
		getChannels()
	}

	private fun observeClient() = viewModelScope.launch {
		clientRepository.getClientFlow().collect { updateState { copy(client = it) } }
	}

	private fun observeItems() = viewModelScope.launch {
		combine(
			bookShelfRepository.getBookShelfFlow(BookShelfState.READING),
			channelRepository.getChannelsFlow(),
			_uiState
		) { bookshelfItems, channels, uiState ->
			groupItems(
				clientNickname = uiState.client.nickname,
				bookshelfItems = bookshelfItems,
				channels = channels,
				bookImgSizeManager = bookImgSizeManager,
				bookUiState = uiState.bookUiState,
				channelUiState = uiState.channelUiState
			)
		}.collect { updateState { copy(items = it) } }
	}

	private fun getReadingBooks() = viewModelScope.launch {
		runCatching { bookShelfRepository.getBookShelfItems(BookShelfState.READING) }
			.onSuccess { updateState { copy(bookUiState = UiState.SUCCESS) } }
			.onFailure {
				startEvent(HomeUiEvent.ShowSnackBar(R.string.error_else))
				updateState { copy(bookUiState = UiState.ERROR) }
			}
	}

	private fun getChannels() = viewModelScope.launch {
		runCatching { channelRepository.getChannels() }
			.onSuccess { updateState { copy(channelUiState = UiState.SUCCESS) } }
			.onFailure {
				startEvent(HomeUiEvent.ShowSnackBar(R.string.error_else))
				updateState { copy(channelUiState = UiState.ERROR) }
			}
	}

	fun onBookItemClick() {
		startEvent(HomeUiEvent.MoveToReadingBookShelf)
	}

	fun onChannelItemClick(channelId: Long) {
		startEvent(HomeUiEvent.MoveToChannel(channelId))
	}

	fun onClickMoveToSearch() {
		startEvent(HomeUiEvent.MoveToSearch)
	}

	fun onClickMakeChannel() {
		startEvent(HomeUiEvent.MoveToMakeChannel)
	}

	fun onClickRetryBookLoadBtn() {
		getReadingBooks()
	}

	fun onClickRetryChannelLoadBtn() {
		getChannels()
	}

	private inline fun updateState(block: HomeUiState.() -> HomeUiState) {
		_uiState.update { _uiState.value.block() }
	}

	fun startEvent(event: HomeUiEvent) = viewModelScope.launch {
		_eventFlow.emit(event)
	}
}