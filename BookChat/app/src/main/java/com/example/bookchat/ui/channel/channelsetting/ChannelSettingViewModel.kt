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

	//여기서 나가는거 까진 괜찮은데 ChannelActivity는 어떻게 나가지 지금 페이지 Fragment로 했어야했나..?
	private fun exitChannel() = viewModelScope.launch {
		runCatching { channelRepository.leaveChannel(channelId) }
			.onSuccess { onClickXBtn() }
			.onFailure { startEvent(ChannelSettingUiEvent.MakeToast(R.string.channel_exit_fail)) }
	}

	private fun changeChannelSetting() = viewModelScope.launch {
		runCatching {
			channelRepository.changeChannelSetting(
				channelId = channelId,
				channelTitle = uiState.value.newTitle,
				channelCapacity = uiState.value.channel.roomCapacity ?: 0, // TODO : 이거 입력받을까?
				//현재 Viewmodel에서 받을까 혹은 dialog단에서 그냥 API호출할까
				//근데 API가 한덩어리 인걸로봐서 그냥 한번에 보내는게 나을 듯
				channelTags = uiState.value.tagList,
				channelImage = uiState.value.newProfileImage
			)
		}
			.onSuccess {
				startEvent(ChannelSettingUiEvent.MoveBack)
				startEvent(ChannelSettingUiEvent.MakeToast(R.string.change_channel_setting_success))
			}
			.onFailure { }
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
		updateState { copy(newProfileImage = profile) }
	}

	fun onClickCameraBtn() {
		startEvent(ChannelSettingUiEvent.PermissionCheck)
	}

	fun onClickApplyBtn() = viewModelScope.launch {
//		if (uiState.value.isPossibleChangeChannel.not()) return@launch
//		changeChannelSetting()
	}

	fun onClickChannelExitDialogBtn() {
		exitChannel()
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