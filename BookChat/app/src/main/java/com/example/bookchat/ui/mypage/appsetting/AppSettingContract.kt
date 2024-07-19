package com.example.bookchat.ui.mypage.appsetting


data class AppSettingUiState(
	val isPushNotificationEnabled: Boolean,
) {

	enum class UiState {
		SUCCESS,
		LOADING,
		ERROR,
	}

	companion object {
		val DEFAULT = AppSettingUiState(
			isPushNotificationEnabled = true
		)
	}
}

sealed class AppSettingUiEvent {
}
