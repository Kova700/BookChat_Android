package com.example.bookchat.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookchat.data.repository.ChattingRepositoryFacade
import com.example.bookchat.ui.viewmodel.contract.ChannelListUiEvent
import com.example.bookchat.ui.viewmodel.contract.ChannelListUiState
import com.example.bookchat.ui.viewmodel.contract.ChannelListUiState.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChannelListViewModel @Inject constructor(
	private val chattingRepositoryFacade: ChattingRepositoryFacade
) : ViewModel() {

	private val _eventFlow = MutableSharedFlow<ChannelListUiEvent>()
	val eventFlow = _eventFlow.asSharedFlow()

	private val _uiStateFlow = MutableStateFlow<ChannelListUiState>(ChannelListUiState.DEFAULT)
	val uiStateFlow = _uiStateFlow.asStateFlow()

	init {
		observeChannels()
		getChannels()
	}

	private fun observeChannels() = viewModelScope.launch {
		chattingRepositoryFacade.getChannelsFlow().collect { newChannels ->
			updateState { copy(channels = newChannels) }
		}
	}

	private fun getChannels() = viewModelScope.launch {
		updateState { copy(uiState = UiState.LOADING) }
		runCatching { chattingRepositoryFacade.getChannels() }
			.onSuccess {
				updateState { copy(uiState = UiState.SUCCESS) }
			}
			.onFailure {
				handleError(it)
				updateState { copy(uiState = UiState.ERROR) }
			}
	}

	fun loadNextChannels(lastVisibleItemPosition: Int) {
		if (uiStateFlow.value.channels.size - 1 > lastVisibleItemPosition ||
			uiStateFlow.value.uiState == UiState.LOADING
		) return
		getChannels()
	}

	fun clickPlusBtn() {
		startEvent(ChannelListUiEvent.MoveToMakeChannelPage)
	}

	private fun startEvent(event: ChannelListUiEvent) = viewModelScope.launch {
		_eventFlow.emit(event)
	}

	private fun handleError(throwable: Throwable) {

	}

	private inline fun updateState(block: ChannelListUiState.() -> ChannelListUiState) {
		_uiStateFlow.update {
			_uiStateFlow.value.block()
		}
	}
}