package com.example.bookchat.ui.channel.channelsetting

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookchat.R
import com.example.bookchat.domain.repository.ChannelRepository
import com.example.bookchat.ui.channel.chatting.ChannelActivity.Companion.EXTRA_CHANNEL_ID
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChannelSettingViewModel @Inject constructor(
	private val savedStateHandle: SavedStateHandle,
	private val channelRepository: ChannelRepository,
) : ViewModel() {
	private val channelId = savedStateHandle.get<Long>(EXTRA_CHANNEL_ID)!!

	private val _eventFlow = MutableSharedFlow<ChannelSettingUiEvent>()
	val eventFlow = _eventFlow.asSharedFlow()

	private val _uiState = MutableStateFlow<ChannelSettingUiState>(ChannelSettingUiState.DEFAULT)
	val uiState get() = _uiState.asStateFlow()

	init {
		initUiState()
	}

	private fun initUiState() = viewModelScope.launch {
		val originChannel = channelRepository.getChannel(channelId)
		updateState {
			copy(
				channel = originChannel,
				newTitle = originChannel.roomName,
				newTags = originChannel.tagsString ?: "",
				newCapacity = originChannel.roomCapacity ?: 0
			)
		}

	}

	private fun exitChannel() = viewModelScope.launch {
		runCatching { channelRepository.leaveChannel(channelId) }
			.onSuccess { startEvent(ChannelSettingUiEvent.ExitChannel) }
			.onFailure { startEvent(ChannelSettingUiEvent.MakeToast(R.string.channel_exit_fail)) }
	}

	//TODO :{"errorCode":"500","message":"예상치 못한 예외가 발생했습니다."} 서버 수정 대기중
	private fun changeChannelSetting() = viewModelScope.launch {
		runCatching {
			channelRepository.changeChannelSetting(
				channelId = channelId,
				channelTitle = uiState.value.newTitle,
				channelTags = uiState.value.tagList,
				channelCapacity = uiState.value.newCapacity,
				channelImage = uiState.value.newProfileImage
			)
		}
			.onSuccess {
				startEvent(ChannelSettingUiEvent.MoveBack)
				startEvent(ChannelSettingUiEvent.MakeToast(R.string.change_channel_setting_success))
			}
			.onFailure { startEvent(ChannelSettingUiEvent.MakeToast(R.string.change_channel_setting_fail)) }
	}

	fun onClickChannelCapacityBtn() {
		startEvent(ChannelSettingUiEvent.ShowChannelCapacityDialog)
	}

	fun onClickHostChangeBtn() {
		startEvent(ChannelSettingUiEvent.MoveHostManage)
	}

	fun onClickSubHostChangeBtn() {
		startEvent(ChannelSettingUiEvent.MoveSubHostManage)
	}

	fun onClickExitChannelBtn() {
		startEvent(ChannelSettingUiEvent.ShowChannelExitWarningDialog)
	}

	fun onChangeChannelTitle(text: String) {
		updateState { copy(newTitle = text) }
	}

	fun onChangeChannelTags(text: String) {
		updateState { copy(newTags = text) }
	}

	fun onClickXBtn() {
		startEvent(ChannelSettingUiEvent.MoveBack)
	}

	fun onChangeChannelProfile(profile: ByteArray) {
		updateState {
			copy(
				newProfileImage = profile,
				isSelectedDefaultImage = false
			)
		}
	}

	fun onClickCameraBtn() {
		startEvent(ChannelSettingUiEvent.ShowProfileEditDialog)
	}

	fun onClickApplyBtn() = viewModelScope.launch {
		if (uiState.value.isPossibleChangeChannel.not()) return@launch
		changeChannelSetting()
	}

	fun onClickChannelExitDialogBtn() {
		exitChannel()
	}

	fun onSelectGallery() {
		startEvent(ChannelSettingUiEvent.MoveToGallery)
	}

	fun onSelectDefaultProfileImage() {
		updateState {
			copy(
				channel = channel.copy(roomImageUri = null),
				newProfileImage = null,
				isSelectedDefaultImage = true
			)
		}
	}

	fun onClickChannelCapacityDialogBtn(newCapacity: Int) {
		updateState { copy(newCapacity = newCapacity) }
	}

	fun onClickChannelTitleClearBtn() {
		updateState { copy(newTitle = "") }
	}

	private inline fun updateState(block: ChannelSettingUiState.() -> ChannelSettingUiState) {
		_uiState.update { _uiState.value.block() }
	}

	private fun startEvent(event: ChannelSettingUiEvent) = viewModelScope.launch {
		_eventFlow.emit(event)
	}

}