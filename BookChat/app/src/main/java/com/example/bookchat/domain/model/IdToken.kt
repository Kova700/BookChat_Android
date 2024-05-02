package com.example.bookchat.domain.model

data class IdToken(
	val token: String,
	val oAuth2Provider: OAuth2Provider
)