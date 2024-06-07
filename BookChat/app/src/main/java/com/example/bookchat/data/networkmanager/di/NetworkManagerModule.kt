package com.example.bookchat.data.networkmanager.di

import android.content.Context
import com.example.bookchat.data.networkmanager.NetworkManagerImpl
import com.example.bookchat.domain.NetworkManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
internal object NetworkManagerModule {

	@Singleton
	@Provides
	fun provideNetworkManager(@ApplicationContext appContext: Context): NetworkManager {
		return NetworkManagerImpl(appContext)
	}

}