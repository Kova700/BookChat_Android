package com.kova700.bookchat.core.data.deviceinfo.internal

import com.kova700.bookchat.core.data.deviceinfo.external.DeviceIDRepository
import com.kova700.core.datastore.deviceinfo.DeviceInfoDataStore
import javax.inject.Inject

class DeviceIDRepositoryImpl @Inject constructor(
	private val dataStore: DeviceInfoDataStore,
) : DeviceIDRepository {

	override suspend fun getDeviceID(): String {
		return dataStore.getDeviceID()
	}

	override suspend fun clear() {
		dataStore.clear()
	}
}