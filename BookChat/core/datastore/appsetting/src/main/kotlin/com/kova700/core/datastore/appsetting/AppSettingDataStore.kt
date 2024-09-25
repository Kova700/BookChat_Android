package com.kova700.core.datastore.appsetting

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.kova700.bookchat.core.datastore.datastore.clearData
import com.kova700.bookchat.core.datastore.datastore.getDataFlow
import com.kova700.bookchat.core.datastore.datastore.setData
import com.kova700.core.data.appsetting.external.model.AppSetting
import com.kova700.core.datastore.appsetting.mapper.toDomain
import com.kova700.core.datastore.appsetting.mapper.toEntity
import com.kova700.core.datastore.appsetting.model.AppSettingEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

class AppSettingDataStore @Inject constructor(
	private val dataStore: DataStore<Preferences>,
	private val jsonSerializer: Json,
) {
	private val appSettingKey = stringPreferencesKey(APP_SETTING_KEY)

	fun getAppSettingFlow(): Flow<AppSetting> {
		return dataStore.getDataFlow(appSettingKey)
			.map { appSettingString ->
				appSettingString?.let {
					jsonSerializer.decodeFromString<AppSettingEntity>(appSettingString).toDomain()
				} ?: AppSetting.DEFAULT
			}
	}

	suspend fun isPushNotificationEnabled(): Boolean {
		return getAppSetting().isPushNotificationEnabled
	}

	suspend fun getAppSetting(): AppSetting {
		val appSettingString =
			dataStore.getDataFlow(appSettingKey).firstOrNull() ?: return AppSetting.DEFAULT
		return jsonSerializer.decodeFromString<AppSettingEntity>(appSettingString).toDomain()
	}

	suspend fun setPushNotificationMuteState(isMute: Boolean) {
		val newAppSetting = getAppSetting().copy(isPushNotificationEnabled = isMute)
		dataStore.setData(appSettingKey, jsonSerializer.encodeToString(newAppSetting.toEntity()))
	}

	suspend fun clear() {
		dataStore.clearData(appSettingKey)
	}

	companion object {
		private const val APP_SETTING_KEY = "APP_SETTING_KEY"
	}
}