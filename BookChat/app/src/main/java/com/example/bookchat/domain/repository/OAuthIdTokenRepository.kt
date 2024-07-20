package com.example.bookchat.domain.repository

import com.example.bookchat.oauth.external.model.IdToken

interface OAuthIdTokenRepository {
	fun getIdToken(): IdToken
	fun saveIdToken(token: IdToken)
	fun clear()
}