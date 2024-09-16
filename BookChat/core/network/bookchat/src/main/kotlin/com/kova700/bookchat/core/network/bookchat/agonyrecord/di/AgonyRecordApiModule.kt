package com.kova700.bookchat.core.network.bookchat.agonyrecord.di

import com.kova700.bookchat.core.network.bookchat.agonyrecord.AgonyRecordApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit

@Module
@InstallIn(SingletonComponent::class)
class AgonyRecordApiModule {

	@Provides
	fun provideBookChatApi(
		retrofit: Retrofit,
	): AgonyRecordApi {
		return retrofit.create(AgonyRecordApi::class.java)
	}
}