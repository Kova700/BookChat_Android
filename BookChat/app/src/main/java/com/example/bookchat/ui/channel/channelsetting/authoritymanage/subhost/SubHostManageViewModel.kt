package com.example.bookchat.ui.channel.channelsetting.authoritymanage.subhost

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookchat.R
import com.example.bookchat.domain.model.ChannelMemberAuthority
import com.example.bookchat.domain.model.User
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
class SubHostManageViewModel @Inject constructor(
	private val savedStateHandle: SavedStateHandle,
	private val channelRepository: ChannelRepository,
) : ViewModel() {
	private val channelId = savedStateHandle.get<Long>(ChannelSettingActivity.EXTRA_CHANNEL_ID)!!

	private val _eventFlow = MutableSharedFlow<SubHostManageUiEvent>()
	val eventFlow = _eventFlow.asSharedFlow()

	private val _uiState = MutableStateFlow(SubHostManageUiState.DEFAULT)
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
						filterTypes = listOf(
							ChannelMemberAuthority.HOST,
							ChannelMemberAuthority.SUB_HOST,
						)
					)
				)
			}
		}.collect()
	}

	private fun updateChannelMemberAuthority(
		targetUserId: Long,
		authority: ChannelMemberAuthority,
		onSuccess: (() -> Unit)? = null,
	) = viewModelScope.launch {
		runCatching {
			channelRepository.updateChannelMemberAuthority(
				channelId = channelId,
				targetUserId = targetUserId,
				channelMemberAuthority = authority,
				needServer = true
			)
		}
			.onSuccess { onSuccess?.invoke() }
			.onFailure { startEvent(SubHostManageUiEvent.MakeToast(R.string.change_channel_sub_host_fail)) }
	}

	fun onClickSubHostDeleteBtn(user: User) {
		updateChannelMemberAuthority(
			targetUserId = user.id,
			authority = ChannelMemberAuthority.GUEST,
		)
	}

	fun onClickXBtn() {
		startEvent(SubHostManageUiEvent.MoveBack)
	}

	fun onClickApplyBtn() {
		val selectedMember = uiState.value.selectedMember ?: return
		updateChannelMemberAuthority(
			targetUserId = selectedMember.id,
			authority = ChannelMemberAuthority.SUB_HOST,
			onSuccess = {
				updateState {
					copy(
						searchKeyword = "",
						selectedMember = null
					)
				}
				startEvent(SubHostManageUiEvent.MoveDeleteSubHost)
			}
		)
	}

	fun onClickSubHostAddItem(item: MemberItem) {
		updateState { copy(selectedMember = item) }
	}

	fun onClickMoveAddSubHost() {
		startEvent(SubHostManageUiEvent.MoveAddSubHost)
	}

	fun onClickMoveDeleteSubHost() {
		startEvent(SubHostManageUiEvent.MoveDeleteSubHost)
	}

	fun onChangeSearchKeyWord(text: String) {
		updateState { copy(searchKeyword = text) }
	}

	fun onClickKeywordClearBtn() {
		updateState { copy(searchKeyword = "") }
	}

	private inline fun updateState(block: SubHostManageUiState.() -> SubHostManageUiState) {
		_uiState.update { _uiState.value.block() }
	}

	private fun startEvent(event: SubHostManageUiEvent) = viewModelScope.launch {
		_eventFlow.emit(event)
	}

}