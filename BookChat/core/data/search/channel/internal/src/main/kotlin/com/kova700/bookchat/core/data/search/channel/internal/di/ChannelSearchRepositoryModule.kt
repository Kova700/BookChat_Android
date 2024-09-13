package com.kova700.bookchat.core.data.search.channel.internal.di

import com.kova700.bookchat.core.data.search.channel.external.ChannelSearchRepository
import com.kova700.bookchat.core.data.search.channel.internal.ChannelSearchRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface ChannelSearchRepositoryModule {
	@Binds
	@Singleton
	fun bindBookSearchRepository(
		repository: ChannelSearchRepositoryImpl,
	): ChannelSearchRepository
}