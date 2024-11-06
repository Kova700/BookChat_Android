package com.kova700.bookchat.feature.splash

data class SplashUiState(
	val uiState: UiState,
	val isNetworkConnected: Boolean,
) {

	val isLoading: Boolean
		get() = uiState == UiState.LOADING

	enum class UiState {
		SUCCESS,
		LOADING,
		ERROR,
	}

	companion object {
		val DEFAULT = SplashUiState(
			uiState = UiState.SUCCESS,
			isNetworkConnected = true,
		)
	}
}

sealed class SplashEvent {
	data object MoveToMain : SplashEvent()
	data object MoveToLogin : SplashEvent()
	data class ShowServerDisabledDialog(val message: String) : SplashEvent()
	data class ShowServerMaintenanceDialog(val message: String) : SplashEvent()
}
