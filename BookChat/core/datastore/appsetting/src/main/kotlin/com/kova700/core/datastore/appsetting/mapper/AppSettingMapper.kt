package com.kova700.core.datastore.appsetting.mapper

import com.kova700.core.data.appsetting.external.model.AppSetting
import com.kova700.core.datastore.appsetting.model.AppSettingEntity

fun AppSetting.toEntity(): AppSettingEntity {
	return AppSettingEntity(
		isPushNotificationEnabled = isPushNotificationEnabled,
	)
}

fun AppSettingEntity.toDomain(): AppSetting {
	return AppSetting(
		isPushNotificationEnabled = isPushNotificationEnabled,
	)
}