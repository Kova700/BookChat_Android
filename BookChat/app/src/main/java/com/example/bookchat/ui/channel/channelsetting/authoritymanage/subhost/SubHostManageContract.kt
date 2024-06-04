package com.example.bookchat.ui.channel.channelsetting.authoritymanage.subhost

import com.example.bookchat.domain.model.Channel
import com.example.bookchat.ui.channel.channelsetting.authoritymanage.model.MemberItem

data class SubHostManageUiState(
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
		val DEFAULT = SubHostManageUiState(
			uiState = UiState.SUCCESS,
			searchKeyword = "",
			channel = Channel.DEFAULT,
			searchedMembers = emptyList(),
			selectedMember = null
		)
	}
}

sealed class SubHostManageUiEvent {
	object MoveBack : SubHostManageUiEvent()
	object MoveAddSubHost : SubHostManageUiEvent()
	object MoveDeleteSubHost : SubHostManageUiEvent()
	data class MakeToast(
		val stringId: Int,
	) : SubHostManageUiEvent()
}