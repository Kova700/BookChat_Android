package com.example.bookchat.oauth.internal

import android.content.Context
import com.example.bookchat.domain.model.OAuth2Provider
import com.example.bookchat.oauth.external.OAuthClient
import com.example.bookchat.oauth.external.model.IdToken
import com.example.bookchat.oauth.external.model.exception.ClientCancelException
import com.example.bookchat.oauth.internal.google.external.GoogleLoginClient
import com.example.bookchat.oauth.internal.google.external.exception.GoogleLoginClientCancelException
import com.example.bookchat.oauth.internal.kakao.external.KakaoLoginClient
import com.example.bookchat.oauth.internal.kakao.external.exception.KakaoLoginClientCancelException
import javax.inject.Inject

class OAuthClientManager @Inject constructor(
	private val kakaoLoginClient: KakaoLoginClient,
	private val googleLoginClient: GoogleLoginClient,
) : OAuthClient {

	override suspend fun login(
		context: Context,
		oauth2Provider: OAuth2Provider,
	): IdToken {
		return runCatching {
			when (oauth2Provider) {
				OAuth2Provider.KAKAO -> kakaoLoginClient.login(context)
				OAuth2Provider.GOOGLE -> googleLoginClient.login(context)
			}
		}.getOrElse {
			if (it is GoogleLoginClientCancelException
				|| it is KakaoLoginClientCancelException
			) throw ClientCancelException()
			else throw it
		}
	}

	override suspend fun logout() {
		TODO("Not yet implemented")
	}

	override suspend fun withdraw() {
		TODO("Not yet implemented")
	}
}