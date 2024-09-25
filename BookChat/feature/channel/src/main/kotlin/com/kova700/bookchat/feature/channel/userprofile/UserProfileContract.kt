package com.kova700.bookchat.feature.channel.userprofile

import com.kova700.bookchat.core.data.channel.external.model.Channel
import com.kova700.bookchat.core.data.channel.external.model.ChannelMemberAuthority
import com.kova700.bookchat.core.data.user.external.model.User

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
	data object MoveBack : UserProfileUiEvent()
	data object ShowUserBanWarningDialog : UserProfileUiEvent()
	data class ShowSnackBar(
		val stringId: Int,
	) : UserProfileUiEvent()
}