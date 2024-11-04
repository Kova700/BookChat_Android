package com.kova700.bookchat.feature.channel.channelsetting

import android.graphics.Bitmap
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kova700.bookchat.core.data.channel.external.repository.ChannelRepository
import com.kova700.bookchat.core.design_system.R
import com.kova700.bookchat.feature.channel.channelsetting.ChannelSettingUiState.UiState
import com.kova700.bookchat.feature.channel.chatting.ChannelActivity.Companion.EXTRA_CHANNEL_ID
import com.kova700.bookchat.util.image.bitmap.compressToByteArray
import com.kova700.core.domain.usecase.channel.GetClientChannelUseCase
import com.kova700.core.domain.usecase.channel.LeaveChannelUseCase
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
	private val getClientChannelUseCase: GetClientChannelUseCase,
	private val leaveChannelUseCase: LeaveChannelUseCase,
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
		val originChannel = getClientChannelUseCase(channelId) ?: return@launch
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
		if (uiState.value.isLoading) return@launch
		updateState { copy(uiState = UiState.LOADING) }
		runCatching { leaveChannelUseCase(channelId) }
			.onSuccess {
				updateState { copy(uiState = UiState.SUCCESS) }
				startEvent(ChannelSettingUiEvent.ExitChannel)
			}
			.onFailure {
				updateState { copy(uiState = UiState.ERROR) }
				startEvent(ChannelSettingUiEvent.ShowSnackBar(R.string.channel_exit_fail))
			}
	}

	private fun changeChannelSetting() = viewModelScope.launch {
		if (uiState.value.isLoading) return@launch
		updateState { copy(uiState = UiState.LOADING) }
		runCatching {
			channelRepository.changeChannelSetting(
				channelId = channelId,
				channelTitle = uiState.value.newTitle,
				channelTags = uiState.value.tagList,
				channelCapacity = uiState.value.newCapacity,
				channelImage = uiState.value.newProfileImage?.compressToByteArray(),
				isProfileChanged = uiState.value.isProfileChanged
			)
		}.onSuccess {
			updateState { copy(uiState = UiState.SUCCESS) }
			startEvent(ChannelSettingUiEvent.MoveBack)
		}.onFailure {
			updateState { copy(uiState = UiState.ERROR) }
			startEvent(ChannelSettingUiEvent.CloseKeyboard)
			startEvent(ChannelSettingUiEvent.ShowSnackBar(R.string.change_channel_setting_fail))
		}
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

	fun onChangeChannelTitle(text: String?) {
		if (text.isNullOrBlank() || text.length > CHANNEL_TITLE_MAX_LENGTH) return
		updateState { copy(newTitle = text) }
	}

	fun onChangeChannelTags(text: String?) {
		if (text.isNullOrBlank() || text.length > CHANNEL_TAG_MAX_LENGTH) return
		updateState { copy(newTags = text) }
	}

	fun onClickXBtn() {
		startEvent(ChannelSettingUiEvent.MoveBack)
	}

	fun onChangeChannelProfile(profile: Bitmap) {
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

	companion object {
		const val CHANNEL_TITLE_MAX_LENGTH = 30
		const val CHANNEL_TAG_MAX_LENGTH = 50
	}

}