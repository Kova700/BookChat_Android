package com.example.bookchat.oauth.external

import android.content.Context
import com.example.bookchat.domain.model.OAuth2Provider
import com.example.bookchat.oauth.external.model.IdToken

interface OAuthClient {
	suspend fun login(
		context: Context,
		oauth2Provider: OAuth2Provider,
	): IdToken

	suspend fun logout()
	suspend fun withdraw()
}