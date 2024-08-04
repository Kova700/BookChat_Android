package com.example.bookchat.fcm.forcedlogout.di

import com.example.bookchat.fcm.forcedlogout.ForcedLogoutManager
import com.example.bookchat.fcm.forcedlogout.ForcedLogoutManagerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface ForcedLogoutManagerModule {

	@Binds
	@Singleton
	fun bindLogoutPushMessageManager(
		logoutPushMessageManager: ForcedLogoutManagerImpl,
	): ForcedLogoutManager
}