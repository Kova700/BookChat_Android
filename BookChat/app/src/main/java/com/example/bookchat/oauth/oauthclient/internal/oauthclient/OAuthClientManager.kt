package com.example.bookchat.oauth.oauthclient.internal.oauthclient

import android.content.Context
import com.example.bookchat.oauth.oauthclient.external.OAuthClient
import com.example.bookchat.oauth.oauthclient.external.exception.ClientCancelException
import com.example.bookchat.oauth.oauthclient.external.model.OAuth2Provider
import com.example.bookchat.oauth.oauthclient.internal.google.external.GoogleLoginClient
import com.example.bookchat.oauth.oauthclient.internal.google.external.exception.GoogleLoginClientCancelException
import com.example.bookchat.oauth.oauthclient.internal.kakao.external.KakaoLoginClient
import com.example.bookchat.oauth.oauthclient.internal.kakao.external.exception.KakaoLoginClientCancelException
import com.example.bookchat.oauth.repository.external.OAuthIdTokenRepository
import javax.inject.Inject

class OAuthClientManager @Inject constructor(
	private val kakaoLoginClient: KakaoLoginClient,
	private val googleLoginClient: GoogleLoginClient,
	private val oAuthIdTokenRepository: OAuthIdTokenRepository,
) : OAuthClient {

	override suspend fun login(
		activityContext: Context,
		oauth2Provider: OAuth2Provider,
	) {
		val idToken = runCatching {
			when (oauth2Provider) {
				OAuth2Provider.KAKAO -> kakaoLoginClient.login(activityContext)
				OAuth2Provider.GOOGLE -> googleLoginClient.login(activityContext)
			}
		}.getOrElse {
			if (it.isClientCancelException()) throw ClientCancelException()
			else throw it
		}
		oAuthIdTokenRepository.saveIdToken(idToken)
	}

	override suspend fun logout(activityContext: Context) {
		kakaoLoginClient.logout()
		googleLoginClient.logout(activityContext)
	}

	override suspend fun withdraw(activityContext: Context) {
		kakaoLoginClient.withdraw()
		googleLoginClient.withdraw(activityContext)
	}

	private fun Throwable.isClientCancelException(): Boolean {
		return this is GoogleLoginClientCancelException
						|| this is KakaoLoginClientCancelException
	}
}