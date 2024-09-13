package com.kova700.bookchat.core.data.channel.internal.di

import com.kova700.bookchat.core.data.channel.external.repository.ChannelRepository
import com.kova700.bookchat.core.data.channel.external.repository.ChannelTempMessageRepository
import com.kova700.bookchat.core.data.channel.internal.ChannelRepositoryImpl
import com.kova700.bookchat.core.data.channel.internal.ChannelTempMessageRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal interface ChannelRepositoryModule {

	@Binds
	@Singleton
	fun bindChannelRepository(repository: ChannelRepositoryImpl): ChannelRepository

	@Binds
	@Singleton
	fun bindChannelTempMessageRepository(repository: ChannelTempMessageRepositoryImpl): ChannelTempMessageRepository
}