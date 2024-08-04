package com.example.bookchat.oauth.oauthclient.internal.oauthclient.di

import com.example.bookchat.oauth.oauthclient.external.OAuthClient
import com.example.bookchat.oauth.oauthclient.internal.oauthclient.OAuthClientManager
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