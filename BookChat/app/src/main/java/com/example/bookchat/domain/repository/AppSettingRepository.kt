package com.example.bookchat.domain.repository

import com.example.bookchat.domain.model.AppSetting
import kotlinx.coroutines.flow.Flow

interface AppSettingRepository {
	suspend fun observeAppSetting(): Flow<AppSetting>
	suspend fun isPushNotificationEnabled(): Boolean
	suspend fun getAppSetting(): AppSetting
	suspend fun setPushNotificationMuteState(isMute: Boolean)
	suspend fun clear()
}