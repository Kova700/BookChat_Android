package com.kova700.core.data.notificationinfo.internal.di

import com.kova700.core.data.notificationinfo.external.repository.ChattingNotificationInfoRepository
import com.kova700.core.data.notificationinfo.internal.ChattingNotificationInfoRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal interface ChattingNotificationInfoRepositoryModule {

	@Binds
	@Singleton
	fun bindChattingNotificationInfoRepository(
		repository: ChattingNotificationInfoRepositoryImpl,
	): ChattingNotificationInfoRepository
}