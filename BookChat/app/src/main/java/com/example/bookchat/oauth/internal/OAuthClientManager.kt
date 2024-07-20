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
		activityContext: Context,
		oauth2Provider: OAuth2Provider,
	): IdToken {
		return runCatching {
			when (oauth2Provider) {
				OAuth2Provider.KAKAO -> kakaoLoginClient.login(activityContext)
				OAuth2Provider.GOOGLE -> googleLoginClient.login(activityContext)
			}
		}.getOrElse {
			if (it is GoogleLoginClientCancelException
				|| it is KakaoLoginClientCancelException
			) throw ClientCancelException()
			else throw it
		}
	}

	override suspend fun logout(activityContext: Context) {
		kakaoLoginClient.logout()
		googleLoginClient.logout(activityContext)
	}

	override suspend fun withdraw(activityContext: Context) {
		kakaoLoginClient.withdraw()
		googleLoginClient.withdraw(activityContext)
	}
}