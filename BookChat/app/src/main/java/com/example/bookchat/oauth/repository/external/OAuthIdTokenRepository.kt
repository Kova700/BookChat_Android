package com.example.bookchat.oauth.repository.external

import com.example.bookchat.oauth.repository.external.model.IdToken

interface OAuthIdTokenRepository {
	fun getIdToken(): IdToken
	fun saveIdToken(token: IdToken)
	fun clear()
}