package com.example.bookchat.oauth.kakao

import android.content.Context
import android.util.Log
import com.example.bookchat.data.response.KakaoLoginFailException
import com.example.bookchat.data.response.KakaoLoginUserCancelException
import com.example.bookchat.domain.model.IdToken
import com.example.bookchat.domain.model.OAuth2Provider.KAKAO
import com.example.bookchat.utils.Constants.TAG
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.AuthError
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.user.UserApiClient
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume

class KakaoLoginClient @Inject constructor(
	private val userApiClient: UserApiClient
){

	suspend fun login(context: Context): IdToken {
		if (userApiClient.isKakaoTalkLoginAvailable(context)) {
			return loginWithKakaoTalk(context)
		}
		return loginWithKakaoAccount(context)
	}

	private suspend fun loginWithKakaoTalk(context: Context): IdToken {
		val oAuthToken = suspendCancellableCoroutine<Result<OAuthToken>> { continuation ->
			userApiClient.loginWithKakaoTalk(context) { token, error ->
				continuation.resume(getTokenResult(token, error))
			}
		}.getOrElse { kakaoLoginFailHandler(it) }
		return IdToken("$ID_TOKEN_PREFIX ${oAuthToken.idToken}", KAKAO)
	}

	private suspend fun loginWithKakaoAccount(context: Context): IdToken {
		val oAuthToken = suspendCancellableCoroutine<Result<OAuthToken>> { continuation ->
			userApiClient.loginWithKakaoAccount(context) { token, error ->
				continuation.resume(getTokenResult(token, error))
			}
		}.getOrElse { kakaoLoginFailHandler(it) }
		return IdToken("$ID_TOKEN_PREFIX ${oAuthToken.idToken}", KAKAO)
	}

	private fun getTokenResult(token: OAuthToken?, error: Throwable?): Result<OAuthToken> {
		return when {
			token != null -> Result.success(token)
			error != null -> Result.failure(error)
			else -> Result.failure(Exception("token and error is null"))
		}
	}

	//카카오 연결 로그아웃 (임시)
	suspend fun logOut() {
		userApiClient.logout { error ->
			error?.let { Log.d(TAG, "KakaoSDK: logout() - 로그아웃 실패. SDK에서 토큰 삭제됨 error : ${error}") }
				?: run { Log.d(TAG, "KakaoSDK: logout() - 로그아웃 성공. SDK에서 토큰 삭제됨") }
		}
	}

	//카카오 연결 탈퇴 (임시)
	suspend fun withdraw() {
		userApiClient.unlink { error ->
			error?.let { Log.d(TAG, "KakaoSDK: unlink() - 연결 끊기 실패. SDK에서 토큰 삭제됨 error : ${error}") }
				?: run { Log.d(TAG, "KakaoSDK: unlink() - 연결 끊기 성공. SDK에서 토큰 삭제됨") }
		}
	}

	private fun kakaoLoginFailHandler(throwable: Throwable): OAuthToken {
		when (throwable) {
			is AuthError -> throw throwable
			is ClientError -> throw KakaoLoginUserCancelException(throwable.message)
			else -> throw KakaoLoginFailException(throwable.message)
		}
	}

	companion object {
		private const val ID_TOKEN_PREFIX = "Bearer"
	}

}