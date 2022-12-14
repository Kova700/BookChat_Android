package com.example.bookchat.kakao

import android.content.Context
import android.util.Log
import com.example.bookchat.data.IdToken
import com.example.bookchat.response.KakaoLoginFailException
import com.example.bookchat.utils.Constants.TAG
import com.example.bookchat.utils.DataStoreManager
import com.example.bookchat.utils.OAuth2Provider
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.user.UserApiClient
import kotlinx.coroutines.*
import kotlin.coroutines.resume

object KakaoSDK {
    private val userApiClient by lazy { UserApiClient.instance }

    suspend fun kakaoLogin(context :Context) {
        val kakaoLoginResult = getKakaoLoginResult(context)
        kakaoLoginResult.onFailure { throw KakaoLoginFailException(it.message) }
        kakaoLoginResult.onSuccess { kakaoLoginResult.map { saveIdToken(it) } }
    }

    private suspend fun getKakaoLoginResult(context :Context) : Result<OAuthToken>{
        val isKakaoTalkLoginAvailable = userApiClient.isKakaoTalkLoginAvailable(context)
        if (isKakaoTalkLoginAvailable)  return kakaoLoginWithKakaoTalk(context)
        return kakaoLoginWithKakaoAccount(context)
    }

    private suspend fun kakaoLoginWithKakaoTalk(context :Context) : Result<OAuthToken>{
        val kakaoTalkLoginResult = suspendCancellableCoroutine<Result<OAuthToken>> { continuation ->
            userApiClient.loginWithKakaoTalk(context) { token, error ->
                continuation.resume(getTokenResult(token, error))
            }
        }
        return kakaoTalkLoginResult
    }

    private suspend fun kakaoLoginWithKakaoAccount(context :Context) : Result<OAuthToken>{
        val kakaoAccountLoginResult = suspendCancellableCoroutine<Result<OAuthToken>> { continuation ->
            userApiClient.loginWithKakaoAccount(context){ token, error ->
                continuation.resume(getTokenResult(token, error))
            }
        }
        return kakaoAccountLoginResult
    }

    private suspend fun saveIdToken(token :OAuthToken){
        Log.d(TAG, "KakaoSDK: saveIdToken() - idToken : ${token.idToken}")
        DataStoreManager.saveIdToken(IdToken("Bearer ${token.idToken}", OAuth2Provider.KAKAO) )
    }

    private fun getTokenResult(token : OAuthToken?, error :Throwable?) :Result<OAuthToken>{
        return when{
            token != null -> Result.success(token)
            error != null -> Result.failure(error)
            else -> Result.failure(Exception("token and error is null"))
        }
    }

    //????????? ?????? ???????????? (??????)
    fun kakaoLogout(){
        userApiClient.logout { error ->
            error?.let { Log.d(TAG, "KakaoSDK: logout() - ???????????? ??????. SDK?????? ?????? ????????? error : ${error}") }
                ?: run { Log.d(TAG, "KakaoSDK: logout() - ???????????? ??????. SDK?????? ?????? ?????????") }
        }
    }

    //????????? ?????? ?????? (??????)
    fun kakaoWithdraw(){
        userApiClient.unlink { error ->
            error?.let { Log.d(TAG, "KakaoSDK: unlink() - ?????? ?????? ??????. SDK?????? ?????? ????????? error : ${error}") }
                ?: run { Log.d(TAG, "KakaoSDK: unlink() - ?????? ?????? ??????. SDK?????? ?????? ?????????") }
        }
    }

}