package com.example.bookchat.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.bookchat.data.datastore.clearData
import com.example.bookchat.data.datastore.getDataFlow
import com.example.bookchat.data.datastore.setData
import com.example.bookchat.domain.model.AppSetting
import com.example.bookchat.domain.repository.AppSettingRepository
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AppSettingRepositoryImpl @Inject constructor(
	private val dataStore: DataStore<Preferences>,
	private val gson: Gson,
) : AppSettingRepository {
	private val appSettingKey = stringPreferencesKey(APP_SETTING_KEY)

	override suspend fun observeAppSetting(): Flow<AppSetting> {
		return dataStore.getDataFlow(appSettingKey)
			.map { appSettingString ->
				appSettingString?.let {
					gson.fromJson(appSettingString, AppSetting::class.java)
				} ?: AppSetting.DEFAULT
			}
	}

	override suspend fun isPushNotificationEnabled(): Boolean {
		return getAppSetting().isPushNotificationEnabled
	}

	override suspend fun getAppSetting(): AppSetting {
		val appSettingString =
			dataStore.getDataFlow(appSettingKey).firstOrNull() ?: return AppSetting.DEFAULT
		return gson.fromJson(appSettingString, AppSetting::class.java)
	}

	override suspend fun setPushNotificationMuteState(isMute: Boolean) {
		val newAppSetting = getAppSetting().copy(isPushNotificationEnabled = isMute)
		dataStore.setData(appSettingKey, gson.toJson(newAppSetting))
	}

	override suspend fun clear() {
		dataStore.clearData(appSettingKey)
	}

	companion object {
		private const val APP_SETTING_KEY = "APP_SETTING_KEY"
	}
}