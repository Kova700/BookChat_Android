package com.kova700.bookchat.core.data.fcm_token.internal.di

import com.google.firebase.messaging.FirebaseMessaging
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseMessagingModule {
	@Provides
	@Singleton
	fun provideFirebaseMessaging(): FirebaseMessaging {
		return FirebaseMessaging.getInstance()
	}
}