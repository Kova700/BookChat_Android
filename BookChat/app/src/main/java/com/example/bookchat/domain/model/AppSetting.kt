package com.example.bookchat.domain.model

data class AppSetting(
	val isPushNotificationEnabled: Boolean,
) {
	companion object {
		val DEFAULT = AppSetting(
			isPushNotificationEnabled = true,
		)
	}
}