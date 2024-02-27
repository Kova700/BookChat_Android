package com.example.bookchat.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookchat.domain.repository.ChannelRepository
import com.example.bookchat.ui.viewmodel.contract.ChannelListUiEvent
import com.example.bookchat.ui.viewmodel.contract.ChannelListUiState
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
	private val channelRepository: ChannelRepository
) : ViewModel() {

	private val _eventFlow = MutableSharedFlow<ChannelListUiEvent>()
	val eventFlow = _eventFlow.asSharedFlow()

	private val _uiStateFlow = MutableStateFlow<ChannelListUiState>(ChannelListUiState.DEFAULT)
	val uiStateFlow = _uiStateFlow.asStateFlow()

	init {
		init()
	}

	private fun init() {
		viewModelScope.launch {
			observeChannels()
		}
	}

	private suspend fun observeChannels() {
		channelRepository.getChannelsFlow().collect { newChannels ->
			_uiStateFlow.update { it.copy(channels = newChannels) }
		}
	}

	fun loadNextChannels(lastVisibleItemPosition: Int) {
		if (
//			uiStateFlow.value._isLoading.value ||
			uiStateFlow.value.channels.size - 1 > lastVisibleItemPosition
		) return

//		_isLoading.update { true }
		viewModelScope.launch {
			runCatching { channelRepository.getChannels() }
		}
	}

	fun clickPlusBtn() {
		startEvent(ChannelListUiEvent.MoveToMakeChannelPage)
	}

	private fun startEvent(event: ChannelListUiEvent) = viewModelScope.launch {
		_eventFlow.emit(event)
	}

}