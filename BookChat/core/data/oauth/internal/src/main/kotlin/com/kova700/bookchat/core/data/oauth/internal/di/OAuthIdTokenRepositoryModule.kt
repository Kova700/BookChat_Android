package com.kova700.bookchat.core.data.oauth.internal.di

import com.kova700.bookchat.core.data.oauth.external.OAuthIdTokenRepository
import com.kova700.bookchat.core.data.oauth.internal.OAuthIdTokenRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface OAuthIdTokenRepositoryModule {
	@Binds
	@Singleton
	fun bindOAuthIdTokenRepository(
		repository: OAuthIdTokenRepositoryImpl,
	): OAuthIdTokenRepository
}