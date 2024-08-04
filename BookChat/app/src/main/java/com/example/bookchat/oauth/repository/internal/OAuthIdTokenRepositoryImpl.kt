package com.example.bookchat.oauth.repository.internal

import com.example.bookchat.oauth.repository.external.model.IdToken
import com.example.bookchat.oauth.repository.external.OAuthIdTokenRepository
import java.io.IOException
import javax.inject.Inject

class OAuthIdTokenRepositoryImpl @Inject constructor(
) : OAuthIdTokenRepository {
	private var cachedIdToken: IdToken? = null

	override fun getIdToken(): IdToken {
		return cachedIdToken ?: throw IOException("OAuthIdToken is null")
	}

	override fun saveIdToken(token: IdToken) {
		cachedIdToken = token
	}

	override fun clear() {
		cachedIdToken = null
	}

}