package com.example.bookchat.kakao

import android.content.Context
import android.util.Log
import com.example.bookchat.utils.Constants.TAG
import com.kakao.sdk.auth.AuthApiClient
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.*
import com.kakao.sdk.user.UserApiClient
import kotlinx.coroutines.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class KakaoSDK(val context :Context) {
    private val userApiClient = UserApiClient.instance
    private val authApiClient = AuthApiClient.instance

    suspend fun login() : Boolean{
        if (isAvailableToken()){
            Log.d(TAG, "KakaoSDK: login() Token O- called")
            return true
        }
        Log.d(TAG, "KakaoSDK: login() Token x- called")
        kakaoLogin()
    }

    fun logout(){
        userApiClient.logout { error ->
            error?.let { Log.d(TAG, "KakaoSDK: logout() - 로그아웃 실패. SDK에서 토큰 삭제됨 error : ${error}") }
                ?: run { Log.d(TAG, "KakaoSDK: logout() - 로그아웃 성공. SDK에서 토큰 삭제됨") }
        }
    }

    //회원 탈퇴
    fun withdraw(){
        userApiClient.unlink { error ->
            error?.let { Log.d(TAG, "KakaoSDK: unlink() - 연결 끊기 실패. SDK에서 토큰 삭제됨 error : ${error}") }
                ?: run { Log.d(TAG, "KakaoSDK: unlink() - 연결 끊기 성공. SDK에서 토큰 삭제됨") }
        }
    }

    fun kakaoLogin() {
        val isKakaoTalkLoginAvailable = userApiClient.isKakaoTalkLoginAvailable(context)
        if (isKakaoTalkLoginAvailable) {
            Log.d(TAG, "KakaoSDK: kakaoLogin() KakaoTalk o- called")
            kakaoLoginWithKakaoTalk()
            return
        }
        Log.d(TAG, "KakaoSDK: kakaoLogin() KakaoTalk x- called")
        kakaoLoginWithKakaoAccount()
    }

    //매번 서버에게 요청을 보내기 전에 토큰 만료여부 확인
    suspend fun isAvailableToken() :Boolean{
        Log.d(TAG, "KakaoSDK: isAvailableToken() - called")
        //accessTokenInfo호출할 때마다 토큰이 갱신된다하지만 확인해봐야함

        val result = suspendCancellableCoroutine<Boolean> { continuation ->
            userApiClient.accessTokenInfo { tokenInfo, error ->
                error?.let { tokenErrorHandler(error) }
                tokenInfo?.let {
                    Log.i(TAG, "토큰 정보" +
                            "\n회원번호: ${tokenInfo.id}" +
                            "\n만료시간: ${tokenInfo.expiresIn} 초" +
                            "\n앱 ID: ${tokenInfo.appId}"
                    )
                    continuation.resume(tokenInfo.expiresIn > 0)
                } ?: continuation.resume(false)
            }
        }
        return result
    }

    fun getIdToken() : String?{
        val idToken = authApiClient.tokenManagerProvider.manager.getToken()?.idToken
        return idToken?.let { "Bearer $idToken" }
    }

    fun tokenErrorHandler(error :Throwable){
        Log.d(TAG, "KakaoSDK: tokenErrorHandler() - error : $error ")
        when{
            isInvalidToken(error) ->{
                //로그인 필요
                Log.d(TAG, "KakaoSDK: tokenErrorHandler() - called (토큰 사용불가 혹은 토큰 없음)")
//                kakaoLogin()
            }
            else -> {}
        }
    }
    //카카오톡 로그인 성공하면 끝
    //카카오톡 로그인 실패하면 에러 핸들링 할 거 있으면 하고 없으면 카카오 계정 로그인 시도
    //카카오 계정 로그인 성공하면 끝
    //카카오 계정 로그인 실패하면
    //로그인 실패로 에러 핸들링

    //로그인 성공해서 토큰을 서버에 넘겨줬는데 가입된 계저이면 유저 정보 가져오고
    //가입되지 않은 계정이면 회원가입 진행

    //로그인 버튼을 눌렀다.
    //유효한 토큰이 있으면 서버로 토큰을 보내 유저 정보를 가져온다.
    //유효한 토큰이 없다면 카카오로부터 다시 토큰을 받아온다.
    //서버한테 토큰을 보내 유저 정보를 다시 가져온다.

    //서버에게 매 요청시 토큰 만료시간 확인 하고 요청 보냄

    //토큰 유효성 검증 부분
    //자동으로 SDK에서 검증해준다는게 뭔말일까


    fun kakaoLoginWithKakaoTalk(){
        userApiClient.loginWithKakaoTalk(context) { token, error ->
            error?.let {
                kakaoLoginErrorHandler(error)
                kakaoLoginWithKakaoAccount()
            }
            token?.let {
                Log.d(TAG, "TestActivity: onCreate() 카카오톡으로 로그인 성공 (${token.accessToken})- called")
                //토큰 저장 성공
            }
        }
    }

    //카카오 계정으로 로그인 예외 처리 (추후 콜백 -> 코루틴으로 리펙토링)
    private val kakaoAcountCallback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
        error?.let {
            Log.d(TAG, "카카오계정으로 로그인 실패 error : $error")
            kakaoLoginErrorHandler(error)
        }
        token?.let {
            Log.d(TAG, "KakaoSDK: kakaoAcountCallback 카카오계정으로 로그인 성공 accessToken : ${token.accessToken}")
            Log.d(TAG, "KakaoSDK: kakaoAcountCallback 카카오계정으로 로그인 성공 refreshToken : ${token.refreshToken}")
            Log.d(TAG, "KakaoSDK: kakaoAcountCallback 카카오계정으로 로그인 성공 idToken : ${token.idToken}")
        }
    }

    fun kakaoLoginWithKakaoAccount(){
        Log.d(TAG, "KakaoSDK: kakaoLoginWithKakaoAccount() - called")
        userApiClient.loginWithKakaoAccount(context, callback = kakaoAcountCallback)
    }

    fun kakaoLoginErrorHandler(error :Throwable) {
        Log.d(TAG, "KakaoSDK: kakaoLoginErrorHandler() - called")
        when{
            isClientCancel(error) -> {
                Log.d(TAG, "KakaoSDK: kakaoLoginErrorHandler() - isClientCancel -called")
            }
            isClientAccessDenied(error) -> {
                Log.d(TAG, "KakaoSDK: kakaoLoginErrorHandler() - isClientAccessDenied - called")
            }
            isNotExistKakaoTalkAcount(error) -> {
                Log.d(TAG, "KakaoSDK: kakaoLoginErrorHandler() - isNotExistKakaoTalkAcount -called")
                kakaoLoginWithKakaoAccount()
            }
            else -> {
                Log.d(TAG, "KakaoSDK: kakaoLoginErrorHandler() - else - called")}
        }
    }

    //무효한 토큰 (정보를 볼 수 없음)
    fun isInvalidToken(error :Throwable) :Boolean{
        return error is KakaoSdkError && error.isInvalidTokenError()
    }

    // 사용자가 카카오톡 설치 후 디바이스 권한 요청 화면에서 로그인을 취소한 경우,
    // 의도적인 로그인 취소로 보고 카카오계정으로 로그인 시도 없이 로그인 취소로 처리 (예: 뒤로 가기)
    private fun isClientCancel(error : Throwable) :Boolean{
        return error is ClientError && error.reason == ClientErrorCause.Cancelled
    }

    //카카오톡에 카카오계정이 로그인 되어있는지?
    private fun isNotExistKakaoTalkAcount(error : Throwable) :Boolean{
        return error is AuthError && error.reason == AuthErrorCause.Unknown
    }

    //사용자가 권한 동의하지 않음
    private fun isClientAccessDenied(error : Throwable) :Boolean{
        return error is AuthError && error.reason == AuthErrorCause.AccessDenied
    }
}