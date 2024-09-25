package com.kova700.bookchat.core.network.bookchat.agony.di

import com.kova700.bookchat.core.network.bookchat.agony.AgonyApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit

@Module
@InstallIn(SingletonComponent::class)
class AgonyApiModule {

	@Provides
	fun provideAgonyApi(
		retrofit: Retrofit,
	): AgonyApi {
		return retrofit.create(AgonyApi::class.java)
	}
}