package com.kova700.bookchat.core.network_manager.internal.di

import com.kova700.bookchat.core.network_manager.external.NetworkManager
import com.kova700.bookchat.core.network_manager.internal.NetworkManagerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal interface NetworkManagerModule {
	@Binds
	@Singleton
	fun bindNetworkManager(
		networkManager: NetworkManagerImpl,
	): NetworkManager
}