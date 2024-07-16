package com.example.bookchat.domain.repository

interface DeviceIDRepository {
	suspend fun getDeviceID(): String
	suspend fun clearDeviceID()
}