package com.example.bookchat.oauth.kakao.external

import android.content.Context
import android.util.Log
import com.example.bookchat.domain.model.OAuth2Provider.KAKAO
import com.example.bookchat.oauth.kakao.external.exception.KakaoLoginFailException
import com.example.bookchat.oauth.kakao.external.exception.KakaoLoginUserCancelException
import com.example.bookchat.oauth.model.IdToken
import com.example.bookchat.oauth.model.IdToken.Companion.ID_TOKEN_PREFIX
import com.example.bookchat.utils.Constants.TAG
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
					if (it.isClientCanceled()) throw KakaoLoginUserCancelException()
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
			if (it.isClientCanceled()) throw KakaoLoginUserCancelException()
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

	//카카오 연결 로그아웃 (임시)
	suspend fun logout() {
		kakaoUserApiClient.logout { error ->
			error?.let { Log.d(TAG, "KakaoSDK: logout() - 로그아웃 실패. SDK에서 토큰 삭제됨 error : ${error}") }
				?: run { Log.d(TAG, "KakaoSDK: logout() - 로그아웃 성공. SDK에서 토큰 삭제됨") }
		}
	}

	//카카오 연결 탈퇴 (임시)
	suspend fun withdraw() {
		kakaoUserApiClient.unlink { error ->
			error?.let { Log.d(TAG, "KakaoSDK: unlink() - 연결 끊기 실패. SDK에서 토큰 삭제됨 error : ${error}") }
				?: run { Log.d(TAG, "KakaoSDK: unlink() - 연결 끊기 성공. SDK에서 토큰 삭제됨") }
		}
	}

	private fun Throwable.isClientCanceled(): Boolean {
		return this is ClientError && reason == ClientErrorCause.Cancelled
	}

	private fun OAuthToken.getIdToken(): IdToken {
		idToken ?: throw KakaoLoginFailException("idToken is null")
		return IdToken("$ID_TOKEN_PREFIX $idToken", KAKAO)
	}

}