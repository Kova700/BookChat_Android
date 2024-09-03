package com.kova700.bookchat.core.data.oauth.external.model

import com.kova700.bookchat.core.oauth.external.model.OAuth2Provider

data class IdToken(
	val token: String,
	val oAuth2Provider: OAuth2Provider
){
	companion object {
		const val ID_TOKEN_PREFIX = "Bearer"
	}
}