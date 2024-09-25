package com.kova700.bookchat.core.network.bookchat.user.di

import com.kova700.bookchat.core.network.bookchat.user.UserApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit

@Module
@InstallIn(SingletonComponent::class)
class UserApiModule {

	@Provides
	fun provideUserApi(
		retrofit: Retrofit,
	): UserApi {
		return retrofit.create(UserApi::class.java)
	}
}