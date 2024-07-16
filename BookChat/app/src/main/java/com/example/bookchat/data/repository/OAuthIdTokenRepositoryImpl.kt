package com.example.bookchat.data.repository

import com.example.bookchat.domain.model.IdToken
import com.example.bookchat.domain.repository.OAuthIdTokenRepository

class OAuthIdTokenRepositoryImpl : OAuthIdTokenRepository {
	private var cachedIdToken: IdToken? = null

	override fun getIdToken(): IdToken? {
		return cachedIdToken
	}

	override fun saveIdToken(token: IdToken) {
		cachedIdToken = token
	}

}