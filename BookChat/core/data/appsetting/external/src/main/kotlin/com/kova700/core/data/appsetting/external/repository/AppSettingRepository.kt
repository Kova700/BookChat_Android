package com.kova700.core.data.appsetting.external.repository

import com.kova700.core.data.appsetting.external.model.AppSetting
import kotlinx.coroutines.flow.Flow

interface AppSettingRepository {
	fun getAppSettingFlow(): Flow<AppSetting>
	suspend fun isPushNotificationEnabled(): Boolean
	suspend fun getAppSetting(): AppSetting
	suspend fun setPushNotificationMuteState(isMute: Boolean)
	suspend fun clear()
}