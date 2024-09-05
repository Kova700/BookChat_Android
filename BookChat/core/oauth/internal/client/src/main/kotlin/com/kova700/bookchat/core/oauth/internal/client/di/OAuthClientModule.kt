package com.kova700.bookchat.core.oauth.internal.client.di

import com.kova700.bookchat.core.oauth.external.OAuthClient
import com.kova700.bookchat.core.oauth.internal.client.OAuthClientManager
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface OAuthClientModule {
	@Binds
	@Singleton
	fun bindOAuthClient(
		oauthClient: OAuthClientManager,
	): OAuthClient
}