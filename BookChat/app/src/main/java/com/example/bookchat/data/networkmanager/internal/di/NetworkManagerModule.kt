package com.example.bookchat.data.networkmanager.internal.di

import com.example.bookchat.data.networkmanager.external.NetworkManager
import com.example.bookchat.data.networkmanager.internal.NetworkManagerImpl
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