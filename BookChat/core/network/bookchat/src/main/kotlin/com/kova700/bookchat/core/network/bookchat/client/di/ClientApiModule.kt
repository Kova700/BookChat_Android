package com.kova700.bookchat.core.network.bookchat.client.di

import com.kova700.bookchat.core.network.bookchat.client.ClientApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit

@Module
@InstallIn(SingletonComponent::class)
class ClientApiModule {

	@Provides
	fun provideClientApi(
		retrofit: Retrofit,
	): ClientApi {
		return retrofit.create(ClientApi::class.java)
	}
}