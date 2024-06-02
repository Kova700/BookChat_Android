package com.example.bookchat.ui.channel.userprofile

import com.example.bookchat.domain.model.Channel
import com.example.bookchat.domain.model.ChannelMemberAuthority
import com.example.bookchat.domain.model.User

data class UserProfileUiState(
	val uiState: UiState,
	val channel: Channel,
	val targetUser: User,
	val client: User,
) {
	private val clientAuthority
		get() = channel.participantAuthorities?.get(client.id)
			?: ChannelMemberAuthority.GUEST

	private val targetUserAuthority
		get() = channel.participantAuthorities?.get(targetUser.id)

	val isClientAdmin
		get() = clientAuthority == ChannelMemberAuthority.HOST
						|| clientAuthority == ChannelMemberAuthority.SUB_HOST

	val isTargetUserExistInChannel
		get() = targetUserAuthority != null

	val isTargetUserHost
		get() = targetUserAuthority == ChannelMemberAuthority.HOST

	val isTargetUserSubHost
		get() = targetUserAuthority == ChannelMemberAuthority.SUB_HOST

	enum class UiState {
		SUCCESS,
		LOADING,
		ERROR,
	}

	companion object {
		val DEFAULT = UserProfileUiState(
			uiState = UiState.SUCCESS,
			channel = Channel.DEFAULT,
			targetUser = User.Default,
			client = User.Default
		)
	}
}

sealed class UserProfileUiEvent {
	object MoveBack : UserProfileUiEvent()
	object ShowUserBanWarningDialog : UserProfileUiEvent()
	data class MakeToast(
		val stringId: Int,
	) : UserProfileUiEvent()
}