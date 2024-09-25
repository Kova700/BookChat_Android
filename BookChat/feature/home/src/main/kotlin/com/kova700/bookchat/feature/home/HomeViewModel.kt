package com.kova700.bookchat.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kova700.bookchat.core.data.bookshelf.external.BookShelfRepository
import com.kova700.bookchat.core.data.bookshelf.external.model.BookShelfState
import com.kova700.bookchat.core.data.client.external.ClientRepository
import com.kova700.bookchat.core.design_system.R
import com.kova700.bookchat.feature.home.HomeUiState.UiState
import com.kova700.bookchat.feature.home.mapper.groupItems
import com.kova700.bookchat.util.book.BookImgSizeManager
import com.kova700.core.domain.usecase.channel.GetClientChannelsFlowUseCase
import com.kova700.core.domain.usecase.channel.GetClientMostActiveChannelsUseCase
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
	private val bookImgSizeManager: BookImgSizeManager,
	private val getClientMostActiveChannelsUseCase: GetClientMostActiveChannelsUseCase,
	private val getClientChannelsFlowUseCase: GetClientChannelsFlowUseCase,
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
			getClientChannelsFlowUseCase(),
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
		runCatching { getClientMostActiveChannelsUseCase() }
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

	private fun startEvent(event: HomeUiEvent) = viewModelScope.launch {
		_eventFlow.emit(event)
	}
}