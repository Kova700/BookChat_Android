package com.kova700.bookchat.core.data.deviceinfo.external

interface DeviceIDRepository {
	suspend fun getDeviceID(): String
	suspend fun clear()
}