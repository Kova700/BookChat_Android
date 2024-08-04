package com.example.bookchat.oauth.oauthclient.external

import android.content.Context
import com.example.bookchat.oauth.oauthclient.external.model.OAuth2Provider

interface OAuthClient {
	suspend fun login(
		activityContext: Context,
		oauth2Provider: OAuth2Provider,
	)

	suspend fun logout(activityContext: Context)
	suspend fun withdraw(activityContext: Context)
}