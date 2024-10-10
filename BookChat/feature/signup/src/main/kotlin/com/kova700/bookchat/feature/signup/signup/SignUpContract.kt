package com.kova700.bookchat.feature.signup.signup

import com.kova700.bookchat.util.user.namecheck.NicknameCheckState

data class SignUpState(
	val uiState: UiState,
	val nickname: String,
	val nicknameCheckState: NicknameCheckState,
	val clientNewImageUri: String?,
) {

	val isLoading: Boolean
		get() = uiState == UiState.LOADING

	enum class UiState {
		SUCCESS,
		LOADING,
		ERROR,
	}

	companion object {
		val DEFAULT = SignUpState(
			uiState = UiState.SUCCESS,
			nickname = "",
			nicknameCheckState = NicknameCheckState.Default,
			clientNewImageUri = null
		)
	}

}


sealed class SignUpEvent {
	data class MoveToSelectTaste(
		val userNickname: String,
		val userProfileUri: String?,
	) : SignUpEvent()

	data object PermissionCheck : SignUpEvent()
	data object MoveToBack : SignUpEvent()

	data class ErrorEvent(val stringId: Int) : SignUpEvent()
	data class UnknownErrorEvent(val message: String) : SignUpEvent()
}
