package com.example.bookchat.oauth.repository.internal.di

import com.example.bookchat.oauth.repository.external.OAuthIdTokenRepository
import com.example.bookchat.oauth.repository.internal.OAuthIdTokenRepositoryImpl
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