package com.example.bookchat.ui.channelList

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookchat.R
import com.example.bookchat.domain.model.Channel
import com.example.bookchat.domain.repository.ChannelRepository
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
	private val channelRepository: ChannelRepository,
) : ViewModel() {

	private val _eventFlow = MutableSharedFlow<ChannelListUiEvent>()
	val eventFlow = _eventFlow.asSharedFlow()

	private val _uiStateFlow = MutableStateFlow<ChannelListUiState>(ChannelListUiState.DEFAULT)
	val uiStateFlow = _uiStateFlow.asStateFlow()

	init {
		observeChannels()
		getChannels() //TODO : 인터넷 끊겨있다가 인터넷 연결되면 매번 다시 호출 Trigger
	}

	private fun observeChannels() = viewModelScope.launch {
		channelRepository.getChannelsFlow().map { groupItems(it) }
			.collect { newChannels -> updateState { copy(channelListItem = newChannels) } }
	}

	//TODO : Mapper로 이전
	private fun groupItems(channels: List<Channel>): List<ChannelListItem> {
		val groupedItems = mutableListOf<ChannelListItem>()
		if (channels.isEmpty()) return groupedItems

		groupedItems.add(ChannelListItem.Header)
		groupedItems.addAll(channels.map { it.toChannelListItem() })
		return groupedItems
	}

	private fun getChannels() = viewModelScope.launch {
		updateState { copy(uiState = UiState.LOADING) }
		runCatching { channelRepository.getChannels() }
			.onSuccess { updateState { copy(uiState = UiState.SUCCESS) } }
			.onFailure { handleError(it) }
	}

	fun loadNextChannels(lastVisibleItemPosition: Int) {
		if (uiStateFlow.value.channelListItem.size - 1 > lastVisibleItemPosition ||
			uiStateFlow.value.uiState == UiState.LOADING
		) return
		getChannels()
	}

	private fun muteChannel(channelId: Long) = viewModelScope.launch {
		runCatching { channelRepository.muteChannel(channelId) }
	}

	private fun unMuteChannel(channelId: Long) = viewModelScope.launch {
		runCatching { channelRepository.unMuteChannel(channelId) }
	}

	private fun topPinChannel(channelId: Long) = viewModelScope.launch {
		runCatching { channelRepository.topPinChannel(channelId) }
	}

	private fun unPinChannel(channelId: Long) = viewModelScope.launch {
		runCatching { channelRepository.unPinChannel(channelId) }
	}

	private fun exitChannel(channelId: Long) = viewModelScope.launch {
		runCatching { channelRepository.leaveChannel(channelId) }
			.onFailure { startEvent(ChannelListUiEvent.MakeToast(R.string.channel_exit_fail)) }
	}

	fun onClickPlusBtn() {
		startEvent(ChannelListUiEvent.MoveToMakeChannelPage)
	}

	fun onClickChannelItem(channelId: Long) {
		startEvent(ChannelListUiEvent.MoveToChannel(channelId))
	}

	fun onLongClickChannelItem(channel: ChannelListItem.ChannelItem) {
		startEvent(ChannelListUiEvent.ShowChannelSettingDialog(channel))
	}

	fun onClickChannelMute(channelId: Long) {
		muteChannel(channelId)
	}

	fun onClickChannelUnMute(channelId: Long) {
		unMuteChannel(channelId)
	}

	fun onClickChannelTopPin(channelId: Long) {
		topPinChannel(channelId)
	}

	fun onClickChannelUnPin(channelId: Long) {
		unPinChannel(channelId)
	}

	fun onClickChannelExit(channelId: Long) {
		exitChannel(channelId)
	}

	private fun handleError(throwable: Throwable) {
		updateState { copy(uiState = UiState.ERROR) }
	}

	private inline fun updateState(block: ChannelListUiState.() -> ChannelListUiState) {
		_uiStateFlow.update { _uiStateFlow.value.block() }
	}

	private fun startEvent(event: ChannelListUiEvent) = viewModelScope.launch {
		_eventFlow.emit(event)
	}

}