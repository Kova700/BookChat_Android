package com.example.bookchat.oauth.repository.external.model

import com.example.bookchat.oauth.oauthclient.external.model.OAuth2Provider

data class IdToken(
	val token: String,
	val oAuth2Provider: OAuth2Provider
){
	companion object {
		const val ID_TOKEN_PREFIX = "Bearer"
	}
}