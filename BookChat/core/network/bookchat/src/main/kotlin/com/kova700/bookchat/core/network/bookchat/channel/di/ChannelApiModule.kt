package com.kova700.bookchat.core.network.bookchat.channel.di

import com.kova700.bookchat.core.network.bookchat.channel.ChannelApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit

@Module
@InstallIn(SingletonComponent::class)
class ChannelApiModule {

	@Provides
	fun provideChannelApi(
		retrofit: Retrofit,
	): ChannelApi {
		return retrofit.create(ChannelApi::class.java)
	}
}