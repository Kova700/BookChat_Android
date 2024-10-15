package com.kova700.bookchat.feature.login

data class LoginUiState(
	val uiState: UiState,
) {

	val isLoading: Boolean
		get() = uiState == UiState.LOADING

	enum class UiState {
		SUCCESS,
		LOADING,
		ERROR,
	}

	companion object {
		val DEFAULT = LoginUiState(
			uiState = UiState.SUCCESS,
		)
	}
}

sealed interface LoginEvent {
	data object MoveToMain : LoginEvent
	data object MoveToSignUp : LoginEvent
	data object ShowDeviceWarning : LoginEvent
	data object StartKakaoLogin : LoginEvent
	data object StartGoogleLogin : LoginEvent

	data class ErrorEvent(val stringId: Int) : LoginEvent
	data class UnknownErrorEvent(val message: String) : LoginEvent
}