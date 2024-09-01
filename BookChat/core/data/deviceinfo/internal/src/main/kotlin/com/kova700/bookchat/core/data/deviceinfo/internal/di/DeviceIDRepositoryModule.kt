package com.kova700.bookchat.core.data.deviceinfo.internal.di

import com.kova700.bookchat.core.data.deviceinfo.external.DeviceIDRepository
import com.kova700.bookchat.core.data.deviceinfo.internal.DeviceIDRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal interface DeviceIDRepositoryModule {

	@Binds
	@Singleton
	fun bindDeviceIDRepository(repository: DeviceIDRepositoryImpl): DeviceIDRepository
}