package com.kova700.bookchat.feature.mypage.setting.appsetting


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
			isPushNotificationEnabled = false,
		)
	}
}

sealed class AppSettingUiEvent {
}
