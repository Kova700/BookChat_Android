package com.kova700.core.datastore.appsetting.model

import kotlinx.serialization.Serializable

@Serializable
data class AppSettingEntity(
	val isPushNotificationEnabled: Boolean,
)