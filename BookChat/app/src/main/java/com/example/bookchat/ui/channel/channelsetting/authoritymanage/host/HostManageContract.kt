package com.example.bookchat.ui.channel.channelsetting.authoritymanage.host

import com.example.bookchat.domain.model.Channel
import com.example.bookchat.ui.channel.channelsetting.authoritymanage.model.MemberItem

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
	object MoveBack : HostManageUiEvent()
	object ShowHostChangeSuccessDialog : HostManageUiEvent()
	data class MakeToast(
		val stringId: Int,
	) : HostManageUiEvent()
}