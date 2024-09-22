package com.kova700.bookchat.feature.channel.channelsetting.authoritymanage.host

import com.kova700.bookchat.core.data.channel.external.model.Channel
import com.kova700.bookchat.feature.channel.channelsetting.authoritymanage.model.MemberItem

data class HostManageUiState(
	val uiState: UiState,
	val searchKeyword: String,
	val channel: Channel,
	val searchedMembers: List<MemberItem>,
	val selectedMember: MemberItem?,
) {
	val isExistSelectedMember
		get() = selectedMember != null

	enum class UiState {
		SUCCESS,
		LOADING,
		ERROR,
	}

	companion object {
		val DEFAULT = HostManageUiState(
			uiState = UiState.SUCCESS,
			searchKeyword = "",
			channel = Channel.DEFAULT,
			searchedMembers = emptyList(),
			selectedMember = null
		)
	}
}

sealed class HostManageUiEvent {
	data object MoveBack : HostManageUiEvent()
	data object ShowHostChangeSuccessDialog : HostManageUiEvent()
	data object CloseKeyboard : HostManageUiEvent()
	data class ShowSnackBar(
		val stringId: Int,
	) : HostManageUiEvent()
}