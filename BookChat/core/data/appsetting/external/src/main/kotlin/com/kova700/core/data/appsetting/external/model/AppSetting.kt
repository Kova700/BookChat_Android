package com.kova700.core.data.appsetting.external.model

data class AppSetting(
	val isPushNotificationEnabled: Boolean,
) {
	companion object {
		val DEFAULT = AppSetting(
			isPushNotificationEnabled = true,
		)
	}
}