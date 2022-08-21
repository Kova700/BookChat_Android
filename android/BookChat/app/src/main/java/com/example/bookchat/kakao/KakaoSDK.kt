package com.example.bookchat.kakao

import android.content.Context
import android.util.Log
import com.example.bookchat.utils.Constants.TAG
import com.kakao.sdk.auth.AuthApiClient
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.*
import com.kakao.sdk.user.UserApiClient

class KakaoSDK(val context :Context) {
    private val userApiClient = UserApiClient.instance
    private val authApiClient = AuthApiClient.instance

    //카카오 계정으로 로그인 예외 처리 (추후 콜백 -> 코루틴으로 리펙토링)
    private val kakaoAcountCallback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
        //if-else 구조 개선
        Log.d(TAG, "kakaoAcountCallback 작동")
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

    fun login(){
        if (hasAccessToken()){
            Log.d(TAG, "KakaoSDK: login() Token O- called")
            bookChatLogin()
            return
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
        val canKakaoTalkLogin = userApiClient.isKakaoTalkLoginAvailable(context)
        if (canKakaoTalkLogin) {
            Log.d(TAG, "KakaoSDK: kakaoLogin() KakaoTalk o- called")
            kakaoLoginWithKakaoTalk()
            return
        }
        Log.d(TAG, "KakaoSDK: kakaoLogin() KakaoTalk x- called")
        kakaoLoginWithKakaoAccount()
    }

    fun bookChatLogin(){
        Log.d(TAG, "KakaoSDK: bookChatLogin() - called")
    }

    fun hasAccessToken() :Boolean{
        //단, hasToken()의 결과가 true라도 현재 사용자가 로그인 상태임을 보장하지 않습니다.
        return authApiClient.hasToken() && isAvailableToken()
    }

    fun isAvailableToken() :Boolean{
        var isAvailable = false
        userApiClient.accessTokenInfo { tokenInfo, error ->
            error?.let { tokenErrorHandler(error)}
                ?: run { //토큰 유효성 체크 성공(필요 시 토큰 갱신됨)
                    tokenInfo?.let {
                        Log.i(TAG, "토큰 정보 보기 성공" +
                                "\n회원번호: ${tokenInfo.id}" +
                                "\n만료시간: ${tokenInfo.expiresIn} 초")
                    }
                    isAvailable = true
                }
        }
        return isAvailable
    }
    fun tokenErrorHandler(error :Throwable){
        when{
            isInvalidToken(error) ->{
                //로그인 필요
                kakaoLogin()
            }
            else -> {}
        }
    }

    fun kakaoLoginWithKakaoTalk(){
        Log.d(TAG, "KakaoSDK: kakaoLoginWithKakaoTalk()1 - called")
        userApiClient.loginWithKakaoTalk(context) { token, error ->
            Log.d(TAG, "KakaoSDK: kakaoLoginWithKakaoTalk()2 - called")
            //if-else 대체재 찾기
            error?.let {
                kakaoLoginErrorHandler(error)
                kakaoLoginWithKakaoAccount()
            }
            token?.let { Log.d(TAG, "TestActivity: onCreate() 카카오톡으로 로그인 성공 (${token.accessToken})- called") }
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