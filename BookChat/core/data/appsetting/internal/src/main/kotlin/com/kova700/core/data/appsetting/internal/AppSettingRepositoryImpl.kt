package com.kova700.core.data.appsetting.internal

import com.kova700.core.data.appsetting.external.model.AppSetting
import com.kova700.core.data.appsetting.external.repository.AppSettingRepository
import com.kova700.core.datastore.appsetting.AppSettingDataStore
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AppSettingRepositoryImpl @Inject constructor(
	private val dataStore: AppSettingDataStore,
) : AppSettingRepository {

	override fun getAppSettingFlow(): Flow<AppSetting> {
		return dataStore.getAppSettingFlow()
	}

	override suspend fun isPushNotificationEnabled(): Boolean {
		return dataStore.isPushNotificationEnabled()
	}

	override suspend fun getAppSetting(): AppSetting {
		return dataStore.getAppSetting()
	}

	override suspend fun setPushNotificationMuteState(isMute: Boolean) {
		dataStore.setPushNotificationMuteState(isMute)
	}

	override suspend fun clear() {
		dataStore.clear()
	}
}