package com.example.bookchat.data.repository

import com.example.bookchat.oauth.model.IdToken
import com.example.bookchat.domain.repository.OAuthIdTokenRepository
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