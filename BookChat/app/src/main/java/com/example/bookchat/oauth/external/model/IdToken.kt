package com.example.bookchat.oauth.external.model

import com.example.bookchat.domain.model.OAuth2Provider

data class IdToken(
	val token: String,
	val oAuth2Provider: OAuth2Provider
){
	companion object {
		const val ID_TOKEN_PREFIX = "Bearer"
	}
}
