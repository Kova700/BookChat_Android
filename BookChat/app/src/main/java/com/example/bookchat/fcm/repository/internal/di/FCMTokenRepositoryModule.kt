package com.example.bookchat.fcm.repository.internal.di

import com.example.bookchat.fcm.repository.external.FCMTokenRepository
import com.example.bookchat.fcm.repository.internal.FCMTokenRepositoryImpl
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