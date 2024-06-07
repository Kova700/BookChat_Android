package com.example.bookchat.ui.channelList

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookchat.R
import com.example.bookchat.domain.NetworkManager
import com.example.bookchat.domain.model.NetworkState
import com.example.bookchat.domain.repository.ChannelRepository
import com.example.bookchat.ui.channelList.ChannelListUiState.UiState
import com.example.bookchat.ui.channelList.mapper.toChannelListItem
import com.example.bookchat.ui.channelList.model.ChannelListItem
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
class ChannelListViewModel @Inject constructor(
	private val channelRepository: ChannelRepository,
	private val networkManager: NetworkManager,
) : ViewModel() {

	private val _eventFlow = MutableSharedFlow<ChannelListUiEvent>()
	val eventFlow = _eventFlow.asSharedFlow()

	private val _uiState = MutableStateFlow<ChannelListUiState>(ChannelListUiState.DEFAULT)
	val uiState = _uiState.asStateFlow()

	private val _isSwiped = MutableStateFlow<Map<Long, Boolean>>(emptyMap()) //(channelId, Boolean)

	init {
		observeChannels()
		getMostActiveChannels()
		observeNetworkState()
	}

	private fun observeNetworkState() = viewModelScope.launch {
		networkManager.getStateFlow().collect { state ->
			updateState { copy(networkState = state) }
			when (state) {
				NetworkState.CONNECTED -> getMostActiveChannels()
				NetworkState.DISCONNECTED -> Unit
			}
		}
	}

	private fun observeChannels() = viewModelScope.launch {
		_isSwiped.combine(channelRepository.getChannelsFlow()) { isSwiped, channels ->
			channels.toChannelListItem(isSwiped)
		}.collect { updateState { copy(channelListItem = it) } }
	}

	private fun getMostActiveChannels() = viewModelScope.launch {
		if (uiState.value.uiState == UiState.LOADING) return@launch
		updateState { copy(uiState = UiState.LOADING) }
		runCatching { channelRepository.getMostActiveChannels() }
			.onSuccess { updateState { copy(uiState = UiState.SUCCESS) } }
			.onFailure { handleError(it) }
	}

	private fun getChannels() = viewModelScope.launch {
		if (uiState.value.uiState == UiState.LOADING) return@launch
		updateState { copy(uiState = UiState.LOADING) }
		runCatching { channelRepository.getChannels() }
			.onSuccess { updateState { copy(uiState = UiState.SUCCESS) } }
			.onFailure { handleError(it) }
	}

	fun loadNextChannels(lastVisibleItemPosition: Int) {
		if (uiState.value.channelListItem.size - 1 > lastVisibleItemPosition
			|| uiState.value.networkState == NetworkState.DISCONNECTED
		) return
		getChannels()
	}

	private fun muteChannel(channelId: Long) = viewModelScope.launch {
		runCatching { channelRepository.muteChannel(channelId) }
			.onSuccess { _isSwiped.update { _isSwiped.value + (channelId to false) } }
	}

	private fun unMuteChannel(channelId: Long) = viewModelScope.launch {
		runCatching { channelRepository.unMuteChannel(channelId) }
			.onSuccess { _isSwiped.update { _isSwiped.value + (channelId to false) } }
	}

	private fun topPinChannel(channelId: Long) = viewModelScope.launch {
		runCatching { channelRepository.topPinChannel(channelId) }
			.onSuccess { _isSwiped.update { _isSwiped.value + (channelId to false) } }
	}

	private fun unPinChannel(channelId: Long) = viewModelScope.launch {
		runCatching { channelRepository.unPinChannel(channelId) }
			.onSuccess { _isSwiped.update { _isSwiped.value + (channelId to false) } }
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

	fun onSwipeChannelItem(channel: ChannelListItem.ChannelItem, isSwiped: Boolean) {
		_isSwiped.update { _isSwiped.value + (channel.roomId to isSwiped) }
	}

	fun onClickMuteRelatedBtn(channel: ChannelListItem.ChannelItem) {
		if (channel.notificationFlag) muteChannel(channel.roomId)
		else unMuteChannel(channel.roomId)
	}

	fun onClickTopPinRelatedBtn(channel: ChannelListItem.ChannelItem) {
		if (channel.isTopPined) unPinChannel(channel.roomId)
		else topPinChannel(channel.roomId)
	}

	fun onClickChannelExit(channelId: Long) {
		exitChannel(channelId)
	}

	private fun handleError(throwable: Throwable) {
		updateState { copy(uiState = UiState.ERROR) }
	}

	private inline fun updateState(block: ChannelListUiState.() -> ChannelListUiState) {
		_uiState.update { _uiState.value.block() }
	}

	private fun startEvent(event: ChannelListUiEvent) = viewModelScope.launch {
		_eventFlow.emit(event)
	}

}