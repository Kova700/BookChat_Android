package com.kova700.bookchat.core.data.oauth.external

import com.kova700.bookchat.core.data.oauth.external.model.IdToken

interface OAuthIdTokenRepository {
	fun getIdToken(): IdToken
	fun saveIdToken(token: IdToken)
	fun clear()
}