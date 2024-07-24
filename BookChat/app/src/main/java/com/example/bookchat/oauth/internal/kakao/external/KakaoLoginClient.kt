package com.example.bookchat.oauth.internal.kakao.external

import android.content.Context
import com.example.bookchat.oauth.external.model.IdToken
import com.example.bookchat.oauth.external.model.IdToken.Companion.ID_TOKEN_PREFIX
import com.example.bookchat.oauth.external.model.OAuth2Provider.KAKAO
import com.example.bookchat.oauth.internal.kakao.external.exception.KakaoLoginClientCancelException
import com.example.bookchat.oauth.internal.kakao.external.exception.KakaoLoginFailException
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class KakaoLoginClient @Inject constructor(
	private val kakaoUserApiClient: UserApiClient,
) {

	suspend fun login(context: Context): IdToken {
		if (kakaoUserApiClient.isKakaoTalkLoginAvailable(context)) {
			return runCatching { loginWithKakaoTalk(context) }
				.getOrElse {
					if (it is KakaoLoginClientCancelException) throw it
					loginWithKakaoAccount(context)
				}
		}
		return loginWithKakaoAccount(context)
	}

	private suspend fun loginWithKakaoTalk(context: Context): IdToken {
		return suspendCancellableCoroutine<IdToken> { continuation ->
			kakaoUserApiClient.loginWithKakaoTalk(context) { token, error ->
				continuation.getLoginResult(token, error)
			}
		}
	}

	private suspend fun loginWithKakaoAccount(context: Context): IdToken {
		return suspendCancellableCoroutine<IdToken> { continuation ->
			kakaoUserApiClient.loginWithKakaoAccount(context) { token, error ->
				continuation.getLoginResult(token, error)
			}
		}
	}

	private fun Continuation<IdToken>.getLoginResult(
		token: OAuthToken?,
		error: Throwable?,
	) {
		when {
			token != null -> resume(token.getIdToken())
			error.isClientCanceled() -> resumeWithException(KakaoLoginClientCancelException())
			error != null -> resumeWithException(error)
			else -> resumeWithException(Exception("token is null"))
		}
	}

	/** API 실패해도 SDK 초기화는 이루어짐*/
	fun logout() {
		kakaoUserApiClient.logout { _ -> }
	}

	/** API 실패해도 SDK 초기화는 이루어짐*/
	fun withdraw() {
		kakaoUserApiClient.unlink { _ -> }
	}

	private fun Throwable?.isClientCanceled(): Boolean {
		return this is ClientError && reason == ClientErrorCause.Cancelled
	}

	private fun OAuthToken.getIdToken(): IdToken {
		idToken ?: throw KakaoLoginFailException("idToken is null")
		return IdToken("$ID_TOKEN_PREFIX $idToken", KAKAO)
	}

}