package com.example.bookchat.oauth.internal.di

import com.example.bookchat.oauth.external.OAuthClient
import com.example.bookchat.oauth.internal.OAuthClientManager
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