package com.example.bookchat.oauth.internal.kakao.external

import android.content.Context
import com.example.bookchat.domain.model.OAuth2Provider.KAKAO
import com.example.bookchat.oauth.external.model.IdToken
import com.example.bookchat.oauth.external.model.IdToken.Companion.ID_TOKEN_PREFIX
import com.example.bookchat.oauth.internal.kakao.external.exception.KakaoLoginClientCancelException
import com.example.bookchat.oauth.internal.kakao.external.exception.KakaoLoginFailException
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume

class KakaoLoginClient @Inject constructor(
	private val kakaoUserApiClient: UserApiClient,
) {

	suspend fun login(context: Context): IdToken {
		return if (kakaoUserApiClient.isKakaoTalkLoginAvailable(context)) {
			loginWithKakaoTalk(context)
				.getOrElse {
					if (it.isClientCanceled()) throw KakaoLoginClientCancelException()
					loginWithKakaoAccount(context)
				}
		} else loginWithKakaoAccount(context)
	}

	private suspend fun loginWithKakaoTalk(context: Context): Result<IdToken> {
		return suspendCancellableCoroutine<Result<IdToken>> { continuation ->
			kakaoUserApiClient.loginWithKakaoTalk(context) { token, error ->
				continuation.resume(getLoginResult(token, error))
			}
		}
	}

	private suspend fun loginWithKakaoAccount(context: Context): IdToken {
		return suspendCancellableCoroutine<Result<IdToken>> { continuation ->
			kakaoUserApiClient.loginWithKakaoAccount(context) { token, error ->
				continuation.resume(getLoginResult(token, error))
			}
		}.getOrElse {
			if (it.isClientCanceled()) throw KakaoLoginClientCancelException()
			else throw KakaoLoginFailException(it.message)
		}
	}

	private fun getLoginResult(
		token: OAuthToken?,
		error: Throwable?,
	): Result<IdToken> {
		return when {
			(token != null) -> Result.success(token.getIdToken())
			error != null -> Result.failure(error)
			else -> Result.failure(Exception("token and error is null"))
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

	private fun Throwable.isClientCanceled(): Boolean {
		return this is ClientError && reason == ClientErrorCause.Cancelled
	}

	private fun OAuthToken.getIdToken(): IdToken {
		idToken ?: throw KakaoLoginFailException("idToken is null")
		return IdToken("$ID_TOKEN_PREFIX $idToken", KAKAO)
	}

}