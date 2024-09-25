package com.kova700.bookchat.core.data.fcm_token.internal.di

import com.kova700.bookchat.core.data.fcm_token.external.FCMTokenRepository
import com.kova700.bookchat.core.data.fcm_token.internal.FCMTokenRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface FCMTokenRepositoryModule {
	@Binds
	@Singleton
	fun bindFCMTokenRepository(
		repository: FCMTokenRepositoryImpl,
	): FCMTokenRepository
}