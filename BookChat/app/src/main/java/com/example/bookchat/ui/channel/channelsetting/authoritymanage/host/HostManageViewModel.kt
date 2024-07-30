package com.example.bookchat.ui.channel.channelsetting.authoritymanage.host

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookchat.R
import com.example.bookchat.domain.model.ChannelMemberAuthority
import com.example.bookchat.domain.repository.ChannelRepository
import com.example.bookchat.ui.channel.channelsetting.ChannelSettingActivity
import com.example.bookchat.ui.channel.channelsetting.authoritymanage.mapper.toMemberItems
import com.example.bookchat.ui.channel.channelsetting.authoritymanage.model.MemberItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HostManageViewModel @Inject constructor(
	private val savedStateHandle: SavedStateHandle,
	private val channelRepository: ChannelRepository,
) : ViewModel() {
	private val channelId = savedStateHandle.get<Long>(ChannelSettingActivity.EXTRA_CHANNEL_ID)!!

	private val _eventFlow = MutableSharedFlow<HostManageUiEvent>()
	val eventFlow = _eventFlow.asSharedFlow()

	private val _uiState = MutableStateFlow(HostManageUiState.DEFAULT)
	val uiState get() = _uiState.asStateFlow()

	init {
		initUiState()
	}

	private fun initUiState() = viewModelScope.launch {
		updateState { copy(channel = channelRepository.getChannel(channelId)) }
		observeChannel()
	}

	private fun observeChannel() = viewModelScope.launch {
		uiState.combine(channelRepository.getChannelFlow(channelId)) { state, channel ->
			updateState {
				copy(
					channel = channel,
					searchedMembers = channel.toMemberItems(
						searchKeyword = searchKeyword,
						selectedMemberId = selectedMember?.id,
						filterTypes = listOf(ChannelMemberAuthority.HOST)
					)
				)
			}
		}.collect()
	}

	private fun updateChannelHost(selectedMemberId: Long) = viewModelScope.launch {
		if (uiState.value.uiState == HostManageUiState.UiState.LOADING) return@launch
		updateState { copy(uiState = HostManageUiState.UiState.LOADING) }
		runCatching {
			channelRepository.updateChannelHost(
				channelId = channelId,
				targetUserId = selectedMemberId,
				needServer = true
			)
		}
			.onSuccess {
				updateState { copy(uiState = HostManageUiState.UiState.SUCCESS) }
				startEvent(HostManageUiEvent.ShowHostChangeSuccessDialog)
			}
			.onFailure { startEvent(HostManageUiEvent.ShowSnackBar(R.string.change_channel_host_fail)) }
	}

	fun onClickXBtn() {
		startEvent(HostManageUiEvent.MoveBack)
	}

	fun onClickApplyBtn() {
		val selectedMember = uiState.value.selectedMember ?: return
		startEvent(HostManageUiEvent.CloseKeyboard)
		updateChannelHost(selectedMember.id)
	}

	fun onClickMember(memberItem: MemberItem) {
		updateState { copy(selectedMember = memberItem) }
	}

	fun onChangeSearchKeyWord(text: String) {
		updateState { copy(searchKeyword = text) }
	}

	fun onClickKeywordClearBtn() {
		updateState { copy(searchKeyword = "") }
	}

	private inline fun updateState(block: HostManageUiState.() -> HostManageUiState) {
		_uiState.update { _uiState.value.block() }
	}

	private fun startEvent(event: HostManageUiEvent) = viewModelScope.launch {
		_eventFlow.emit(event)
	}

}