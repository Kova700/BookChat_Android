package com.kova700.bookchat.feature.channellist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kova700.bookchat.core.data.channel.external.model.ChannelMemberAuthority
import com.kova700.bookchat.core.data.channel.external.repository.ChannelRepository
import com.kova700.bookchat.core.design_system.R
import com.kova700.bookchat.core.network_manager.external.NetworkManager
import com.kova700.bookchat.core.network_manager.external.model.NetworkState
import com.kova700.bookchat.feature.channellist.ChannelListUiState.UiState
import com.kova700.bookchat.feature.channellist.mapper.toChannelListItem
import com.kova700.bookchat.feature.channellist.model.ChannelListItem
import com.kova700.core.domain.usecase.channel.GetClientChannelsFlowUseCase
import com.kova700.core.domain.usecase.channel.GetClientChannelsUseCase
import com.kova700.core.domain.usecase.channel.GetClientMostActiveChannelsUseCase
import com.kova700.core.domain.usecase.channel.LeaveChannelUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChannelListViewModel @Inject constructor(
	private val channelRepository: ChannelRepository,
	private val networkManager: NetworkManager,
	private val getClientChannelsUseCase: GetClientChannelsUseCase,
	private val getClientMostActiveChannelsUseCase: GetClientMostActiveChannelsUseCase,
	private val leaveChannelUseCase: LeaveChannelUseCase,
	private val getClientChannelsFlowUseCase: GetClientChannelsFlowUseCase
) : ViewModel() {

	private val _eventFlow = MutableSharedFlow<ChannelListUiEvent>()
	val eventFlow = _eventFlow.asSharedFlow()

	private val _uiState = MutableStateFlow<ChannelListUiState>(ChannelListUiState.DEFAULT)
	val uiState = _uiState.asStateFlow()

	private val _isSwiped = MutableStateFlow<Map<Long, Boolean>>(emptyMap()) //(channelId, Boolean)

	init {
		initUiState()
	}

	private fun initUiState() {
		getInitChannels()
		observeChannels()
		observeNetworkState()
	}

	private fun observeNetworkState() = viewModelScope.launch {
		networkManager.getStateFlow()
			.onEach { updateState { copy(networkState = it) } }
			.drop(1)
			.collect { state ->
				when (state) {
					NetworkState.CONNECTED -> getInitChannels()
					NetworkState.DISCONNECTED -> Unit
				}
			}
	}

	private fun observeChannels() = viewModelScope.launch {
		combine(
			_isSwiped,
			getClientChannelsFlowUseCase(),
			uiState.map { it.uiState }.distinctUntilChanged()
		) { isSwiped, channels, uiState ->
			channels.toChannelListItem(
				isSwipedMap = isSwiped,
				uiState = uiState
			)
		}.collect { updateState { copy(channelListItem = it) } }
	}

	private fun getInitChannels() = viewModelScope.launch {
		if (uiState.value.isInitLoading) return@launch
		updateState { copy(uiState = UiState.INIT_LOADING) }
		runCatching { getClientMostActiveChannelsUseCase() }
			.onSuccess { updateState { copy(uiState = UiState.SUCCESS) } }
			.onFailure {
				if (uiState.value.channelListItem.isEmpty()) updateState { copy(uiState = UiState.INIT_ERROR) }
				else updateState { copy(uiState = UiState.SUCCESS) }
				startEvent(ChannelListUiEvent.ShowSnackBar(R.string.error_else))
			}
	}

	private fun getChannels() = viewModelScope.launch {
		if (uiState.value.isPageLoading) return@launch
		updateState { copy(uiState = UiState.PAGING_LOADING) }
		runCatching { getClientChannelsUseCase() }
			.onSuccess { updateState { copy(uiState = UiState.SUCCESS) } }
			.onFailure { updateState { copy(uiState = UiState.PAGING_ERROR) } }
	}

	fun loadNextChannels(lastVisibleItemPosition: Int) {
		if (uiState.value.channelListItem.size - 1 > lastVisibleItemPosition
			|| uiState.value.networkState == NetworkState.DISCONNECTED
			|| uiState.value.isLoading
		) return
		getChannels()
	}

	private fun muteChannel(channelId: Long) = viewModelScope.launch {
		runCatching { channelRepository.muteChannel(channelId) }
			.onSuccess { _isSwiped.update { _isSwiped.value - channelId } }
	}

	private fun unMuteChannel(channelId: Long) = viewModelScope.launch {
		runCatching { channelRepository.unMuteChannel(channelId) }
			.onSuccess { _isSwiped.update { _isSwiped.value - channelId } }
	}

	private fun topPinChannel(channelId: Long) = viewModelScope.launch {
		runCatching { channelRepository.topPinChannel(channelId) }
			.onSuccess { _isSwiped.update { _isSwiped.value - channelId } }
	}

	private fun unPinChannel(channelId: Long) = viewModelScope.launch {
		runCatching { channelRepository.unPinChannel(channelId) }
			.onSuccess { _isSwiped.update { _isSwiped.value - channelId } }
	}

	private fun exitChannel(channelId: Long) = viewModelScope.launch {
		runCatching { leaveChannelUseCase(channelId) }
			.onFailure { startEvent(ChannelListUiEvent.ShowSnackBar(R.string.channel_exit_fail)) }
	}

	fun onClickPagingRetry() {
		getChannels()
	}

	fun onClickInitRetry() {
		getInitChannels()
	}

	fun onClickPlusBtn() {
		startEvent(ChannelListUiEvent.MoveToMakeChannelPage)
	}

	fun onClickChannelItem(channelId: Long) {
		startEvent(ChannelListUiEvent.MoveToChannel(channelId))
	}

	//TODO : ChannelResponse에 호스트 정보 반영되면 호스트 유무 반영해서 전달
	fun onLongClickChannelItem(channel: ChannelListItem.ChannelItem) {
		startEvent(
			ChannelListUiEvent.ShowChannelSettingDialog(
				clientAuthority = ChannelMemberAuthority.GUEST,
				channel = channel
			)
		)
	}

	fun onClickChannelSearchBtn() {
		startEvent(ChannelListUiEvent.MoveToSearchChannelPage)
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

	private inline fun updateState(block: ChannelListUiState.() -> ChannelListUiState) {
		_uiState.update { _uiState.value.block() }
	}

	private fun startEvent(event: ChannelListUiEvent) = viewModelScope.launch {
		_eventFlow.emit(event)
	}

}