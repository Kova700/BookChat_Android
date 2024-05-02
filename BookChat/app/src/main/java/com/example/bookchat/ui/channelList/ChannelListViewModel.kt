package com.example.bookchat.ui.channelList

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookchat.data.repository.ChattingRepositoryFacade
import com.example.bookchat.domain.model.Channel
import com.example.bookchat.ui.channelList.ChannelListUiState.UiState
import com.example.bookchat.ui.channelList.mapper.toChannelListItem
import com.example.bookchat.ui.channelList.model.ChannelListItem
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
		chattingRepositoryFacade.getChannelsFlow().map { groupItems(it) }
			.collect { newChannels -> updateState { copy(channelListItem = newChannels) } }
	}

	private fun groupItems(channels: List<Channel>): List<ChannelListItem> {
		val groupedItems = mutableListOf<ChannelListItem>()
		if (channels.isEmpty()) return groupedItems

		groupedItems.add(ChannelListItem.Header)
		groupedItems.addAll(channels.map { it.toChannelListItem() })
		return groupedItems
	}

	private fun getChannels() = viewModelScope.launch {
		updateState { copy(uiState = UiState.LOADING) }
		runCatching { chattingRepositoryFacade.getChannels() }
			.onSuccess { updateState { copy(uiState = UiState.SUCCESS) } }
			.onFailure { handleError(it) }
	}

	fun loadNextChannels(lastVisibleItemPosition: Int) {
		if (uiStateFlow.value.channelListItem.size - 1 > lastVisibleItemPosition ||
			uiStateFlow.value.uiState == UiState.LOADING
		) return
		getChannels()
	}

	fun clickPlusBtn() {
		startEvent(ChannelListUiEvent.MoveToMakeChannelPage)
	}

	fun onChannelItemClick(channelId: Long) {
		startEvent(ChannelListUiEvent.MoveToChannel(channelId))
	}

	private fun startEvent(event: ChannelListUiEvent) = viewModelScope.launch {
		_eventFlow.emit(event)
	}

	private fun handleError(throwable: Throwable) {
		updateState { copy(uiState = UiState.ERROR) }
	}

	private inline fun updateState(block: ChannelListUiState.() -> ChannelListUiState) {
		_uiStateFlow.update {
			_uiStateFlow.value.block()
		}
	}
}