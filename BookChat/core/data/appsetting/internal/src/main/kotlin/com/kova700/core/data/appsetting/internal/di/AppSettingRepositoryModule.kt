package com.kova700.core.data.appsetting.internal.di

import com.kova700.core.data.appsetting.external.repository.AppSettingRepository
import com.kova700.core.data.appsetting.internal.AppSettingRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal interface AppSettingRepositoryModule {

	@Binds
	@Singleton
	fun bindAppSettingRepository(repository: AppSettingRepositoryImpl): AppSettingRepository
}