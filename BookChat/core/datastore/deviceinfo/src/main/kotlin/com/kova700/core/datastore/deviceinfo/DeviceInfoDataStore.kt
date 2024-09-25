package com.kova700.core.datastore.deviceinfo

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.kova700.bookchat.core.datastore.datastore.clearData
import com.kova700.bookchat.core.datastore.datastore.getDataFlow
import com.kova700.bookchat.core.datastore.datastore.setData
import kotlinx.coroutines.flow.firstOrNull
import java.util.UUID
import javax.inject.Inject

class DeviceInfoDataStore @Inject constructor(
	private val dataStore: DataStore<Preferences>,
) {
	private val deviceUUIDKey = stringPreferencesKey(DEVICE_UUID_KEY)

	suspend fun getDeviceID(): String {
		return dataStore.getDataFlow(deviceUUIDKey).firstOrNull() ?: getNewDeviceID()
	}

	private suspend fun getNewDeviceID(): String {
		val uuid = UUID.randomUUID().toString()
		dataStore.setData(deviceUUIDKey, uuid)
		return uuid
	}

	suspend fun clear() {
		dataStore.clearData(deviceUUIDKey)
	}

	companion object {
		private const val DEVICE_UUID_KEY = "DEVICE_UUID_KEY"
	}
}