package com.example.bookchat.kakao

import com.kakao.sdk.user.UserApiClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@InstallIn(SingletonComponent::class)
@Module
object KakaoModule {

	@Singleton
	@Provides
	fun provideUserApiClient(): UserApiClient {
		return UserApiClient.instance
	}

	@Singleton
	@Provides
	fun provideKakaoLoginClient(userApiClient: UserApiClient): KakaoLoginClient {
		return KakaoLoginClient(userApiClient)
	}
}