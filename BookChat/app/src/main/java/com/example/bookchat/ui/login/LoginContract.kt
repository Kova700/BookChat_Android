package com.example.bookchat.ui.login

import com.example.bookchat.domain.model.IdToken

data class LoginUiState(
	val uiState: UiState,
	val idToken: IdToken?,
) {

	enum class UiState {
		SUCCESS,
		LOADING,
		ERROR,
	}

	companion object {
		val DEFAULT = LoginUiState(
			uiState = UiState.SUCCESS,
			idToken = null
		)
	}
}

sealed interface LoginEvent {
	object MoveToMain : LoginEvent
	object MoveToSignUp : LoginEvent
	object ShowDeviceWarning : LoginEvent
	object StartKakaoLogin : LoginEvent
	object StartGoogleLogin : LoginEvent

	data class ErrorEvent(val stringId: Int) : LoginEvent
	data class UnknownErrorEvent(val message: String) : LoginEvent
}