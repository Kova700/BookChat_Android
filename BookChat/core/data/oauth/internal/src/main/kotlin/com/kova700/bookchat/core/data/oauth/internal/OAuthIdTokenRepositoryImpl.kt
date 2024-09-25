package com.kova700.bookchat.core.data.oauth.internal

import com.kova700.bookchat.core.data.oauth.external.OAuthIdTokenRepository
import com.kova700.bookchat.core.data.oauth.external.model.IdToken
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