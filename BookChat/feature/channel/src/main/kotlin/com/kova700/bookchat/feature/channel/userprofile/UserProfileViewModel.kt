package com.kova700.bookchat.feature.channel.userprofile

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kova700.bookchat.core.data.channel.external.repository.ChannelRepository
import com.kova700.bookchat.core.data.client.external.ClientRepository
import com.kova700.bookchat.core.data.user.external.repository.UserRepository
import com.kova700.bookchat.core.design_system.R
import com.kova700.bookchat.feature.channel.chatting.ChannelActivity.Companion.EXTRA_CHANNEL_ID
import com.kova700.bookchat.feature.channel.chatting.ChannelActivity.Companion.EXTRA_USER_ID
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserProfileViewModel @Inject constructor(
	private val savedStateHandle: SavedStateHandle,
	private val channelRepository: ChannelRepository,
	private val userRepository: UserRepository,
	private val clientRepository: ClientRepository,
) : ViewModel() {
	private val targetUserId = savedStateHandle.get<Long>(EXTRA_USER_ID)!!
	private val channelId = savedStateHandle.get<Long>(EXTRA_CHANNEL_ID)!!

	private val _eventFlow = MutableSharedFlow<UserProfileUiEvent>()
	val eventFlow get() = _eventFlow

	private val _uiState = MutableStateFlow<UserProfileUiState>(UserProfileUiState.DEFAULT)
	val uiState get() = _uiState.asStateFlow()

	init {
		initUiState()
	}

	private fun initUiState() = viewModelScope.launch {
		updateState {
			copy(
				channel = channelRepository.getChannel(channelId),
				targetUser = userRepository.getUser(targetUserId),
				client = clientRepository.getClientProfile()
			)
		}
	}

	fun onClickUserBanBtn() {
		if (uiState.value.isClientAdmin.not()) return
		startEvent(UserProfileUiEvent.ShowUserBanWarningDialog)
	}

	private fun banUser() = viewModelScope.launch {
		if ((uiState.value.isClientAdmin
							&& uiState.value.isTargetUserHost.not()
							&& uiState.value.isTargetUserExistInChannel
							&& (uiState.value.targetUser.id != uiState.value.client.id)).not()
		) return@launch

		runCatching {
			channelRepository.banChannelMember(
				channelId = channelId,
				targetUserId = targetUserId,
				needServer = true
			)
		}
			.onSuccess { startEvent(UserProfileUiEvent.MoveBack) }
			.onFailure { startEvent(UserProfileUiEvent.ShowSnackBar(R.string.user_ban_fail)) }
	}

	fun onClickUserBanDialogBtn() {
		banUser()
	}

	fun onClickBackBtn() {
		startEvent(UserProfileUiEvent.MoveBack)
	}

	private fun startEvent(event: UserProfileUiEvent) = viewModelScope.launch {
		eventFlow.emit(event)
	}

	private inline fun updateState(block: UserProfileUiState.() -> UserProfileUiState) {
		_uiState.update { _uiState.value.block() }
	}
}