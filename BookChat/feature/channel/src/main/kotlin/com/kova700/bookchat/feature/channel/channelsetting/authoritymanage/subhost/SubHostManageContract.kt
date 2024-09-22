package com.kova700.bookchat.feature.channel.channelsetting.authoritymanage.subhost

import com.kova700.bookchat.core.data.channel.external.model.Channel
import com.kova700.bookchat.feature.channel.channelsetting.authoritymanage.model.MemberItem

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
	data object MoveBack : SubHostManageUiEvent()
	data object MoveAddSubHost : SubHostManageUiEvent()
	data object MoveDeleteSubHost : SubHostManageUiEvent()
	data class ShowSnackBar(
		val stringId: Int,
	) : SubHostManageUiEvent()
}