package com.kova700.bookchat.core.fcm.forced_logout.di

import com.kova700.bookchat.core.fcm.forced_logout.ForcedLogoutManager
import com.kova700.bookchat.core.fcm.forced_logout.ForcedLogoutManagerImpl
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