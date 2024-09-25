package com.kova700.bookchat.core.oauth.internal.kakao.internal

import com.kakao.sdk.user.UserApiClient
import com.kova700.bookchat.core.oauth.internal.kakao.external.KakaoLoginClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@InstallIn(SingletonComponent::class)
@Module
object KakaoLoginClientModule {

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