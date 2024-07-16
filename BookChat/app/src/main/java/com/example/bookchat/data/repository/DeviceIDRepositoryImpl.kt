package com.example.bookchat.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.bookchat.data.datastore.clearData
import com.example.bookchat.data.datastore.getDataFlow
import com.example.bookchat.data.datastore.setData
import com.example.bookchat.domain.repository.DeviceIDRepository
import kotlinx.coroutines.flow.firstOrNull
import java.util.UUID
import javax.inject.Inject

class DeviceIDRepositoryImpl @Inject constructor(
	private val dataStore: DataStore<Preferences>,
) : DeviceIDRepository {
	private val deviceUUIDKey = stringPreferencesKey(DEVICE_UUID_KEY)

	override suspend fun getDeviceID(): String {
		return dataStore.getDataFlow(deviceUUIDKey).firstOrNull() ?: getNewDeviceID()
	}

	private suspend fun getNewDeviceID(): String {
		val uuid = UUID.randomUUID().toString()
		dataStore.setData(deviceUUIDKey, uuid)
		return uuid
	}

	override suspend fun clearDeviceID() {
		dataStore.clearData(deviceUUIDKey)
	}

	companion object {
		private const val DEVICE_UUID_KEY = "DEVICE_UUID_KEY"
	}
}