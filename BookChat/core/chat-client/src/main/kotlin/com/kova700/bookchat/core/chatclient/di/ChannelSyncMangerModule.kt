package com.kova700.bookchat.core.chatclient.di

import com.kova700.bookchat.core.chatclient.ChannelSyncManger
import com.kova700.bookchat.core.chatclient.ChannelSyncMangerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface ChannelSyncMangerModule {
	@Binds
	@Singleton
	fun bindChannelSyncManger(
		channelSyncManger: ChannelSyncMangerImpl,
	): ChannelSyncManger
}