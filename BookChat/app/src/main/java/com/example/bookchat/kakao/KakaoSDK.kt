package com.example.bookchat.kakao

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.example.bookchat.App
import com.example.bookchat.data.IdToken
import com.example.bookchat.utils.Constants.TAG
import com.example.bookchat.utils.DataStoreManager
import com.example.bookchat.utils.OAuth2Provider
import com.kakao.sdk.auth.AuthApiClient
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.*
import com.kakao.sdk.user.UserApiClient
import com.kakao.sdk.user.model.AccessTokenInfo
import kotlinx.coroutines.*
import kotlin.coroutines.resume

object KakaoSDK {
    private val userApiClient by lazy { UserApiClient.instance }
    private val authApiClient by lazy { AuthApiClient.instance }

    /* ID토큰사용 -> 북챗 토큰 사용으로 정책수정으로 인한 코드 수정 필요 */

    //매번 서버에게 요청을 보내기 전에 토큰 만료여부 확인 만료시에는 카카오서버 갱신 로직에의해 자동 갱신
    suspend fun isAvailableToken() :Boolean{
        val tokenInfoResult = suspendCancellableCoroutine<Result<AccessTokenInfo>> { continuation ->
            userApiClient.accessTokenInfo { tokenInfo, error ->
                continuation.resume(getTokenInfoResult(tokenInfo, error))
            }
        }
        Log.d(TAG, "KakaoSDK: isAvailableToken() - tokenInfoResult : $tokenInfoResult")
//        if (tokenInfoResult.isFailure) tokenInfoErrorHandler(tokenInfoResult.exceptionOrNull())
        return tokenInfoResult.isSuccess
    }

    suspend fun kakaoLogin(context : Context) {
        val isKakaoTalkLoginAvailable =
            userApiClient.isKakaoTalkLoginAvailable(context)
        if (isKakaoTalkLoginAvailable) {
            kakaoLoginWithKakaoTalk(context)
            return
        }
        kakaoLoginWithKakaoAccount(context)
    }

    private suspend fun kakaoLoginWithKakaoTalk(context :Context){
        val kakaoTalkLoginResult = suspendCancellableCoroutine<Result<OAuthToken>> { continuation ->
            userApiClient.loginWithKakaoTalk(context) { token, error ->
                continuation.resume(getTokenResult(token, error))
            }
        }

        if(kakaoTalkLoginResult.isFailure) throw Exception("kakaoTalkLoginResult is Fail(${ kakaoTalkLoginResult.onFailure { it.cause } })")
        Log.d(TAG, "KakaoSDK: kakaoLoginWithKakaoTalk() kakaoTalkLoginResult : $kakaoTalkLoginResult- called")
        if (kakaoTalkLoginResult.isSuccess){ kakaoTalkLoginResult.map { tokenLoging(it)} } //테스트용 로그
    }

    suspend fun kakaoLoginWithKakaoAccount(context :Context){
        val kakaoAccountLoginResult = suspendCancellableCoroutine<Result<OAuthToken>> { continuation ->
            userApiClient.loginWithKakaoAccount(context){ token, error ->
                continuation.resume(getTokenResult(token, error))
            }
        }
        if(kakaoAccountLoginResult.isFailure) throw Exception("kakaoAccountLoginResult is Fail(${ kakaoAccountLoginResult.onFailure { it.message } })")
        if (kakaoAccountLoginResult.isSuccess){ kakaoAccountLoginResult.map { tokenLoging(it)} } //테스트용 로그
    }

    //테스트용 로그
    private suspend fun tokenLoging(token :OAuthToken){

        Log.d(TAG, "KakaoSDK: kakaoAcountCallback 카카오 로그인 성공 accessToken : ${token.accessToken}")
        Log.d(TAG, "KakaoSDK: kakaoAcountCallback 카카오 로그인 성공 refreshToken : ${token.refreshToken}")
        Log.d(TAG, "KakaoSDK: kakaoAcountCallback 카카오 로그인 성공 idToken : ${token.idToken}")
        //id토큰 캐싱해야함
        DataStoreManager.saveIdToken(IdToken("Bearer ${token.idToken}", OAuth2Provider.KAKAO) )
    }

    private fun getTokenResult(token : OAuthToken?, error :Throwable?) :Result<OAuthToken>{
        return when{
            token != null -> Result.success(token)
            error != null -> Result.failure(error)
            else -> Result.failure(Exception("token and error is null"))
        }
    }

    private fun getTokenInfoResult(tokenInfo : AccessTokenInfo?, error :Throwable?) :Result<AccessTokenInfo>{
        return when{
            error != null -> Result.failure(error)
            tokenInfo == null -> Result.failure(Exception("Token info is null"))
            tokenInfo.expiresIn <= 0 -> Result.failure(Exception("Token is expired"))
            else -> Result.success(tokenInfo)
        }
    }

    //성공 여부로 분기 작업해야함
    suspend fun login(){
        if (isAvailableToken()){
            Log.d(TAG, "KakaoSDK: login() Token O- called")
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

    private fun kakaoLoginErrorHandler(error :Throwable?) {
        when{
            isClientCancel(error) -> {
                Toast.makeText(App.instance.applicationContext,"카카오 로그인 실패 - 사용자 취소", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "KakaoSDK: kakaoLoginErrorHandler() - isClientCancel -called")
            }
            isClientAccessDenied(error) -> {
                Toast.makeText(App.instance.applicationContext,"카카오 로그인 실패 - 사용자 접근 거부", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "KakaoSDK: kakaoLoginErrorHandler() - isClientAccessDenied - called")
            }
            isNotExistKakaoTalkAcount(error) -> {
                Toast.makeText(App.instance.applicationContext,"카카오 로그인 실패 - 카카오톡에 로그인된 계정이 없음",
                    Toast.LENGTH_SHORT).show()
                Log.d(TAG, "KakaoSDK: kakaoLoginErrorHandler() - isNotExistKakaoTalkAcount -called")
            }
            else -> {
                Toast.makeText(App.instance.applicationContext,"카카오 로그인 실패 - 잠시 후 다시 시도해 주세요.",
                    Toast.LENGTH_SHORT).show()
                Log.d(TAG, "KakaoSDK: kakaoLoginErrorHandler() - else - called")
            }
        }
    }

    private fun tokenInfoErrorHandler(error :Throwable?){
        when{
            isInvalidToken(error) ->{
                Log.d(TAG, "KakaoSDK: tokenInfoErrorHandler() - called (토큰 사용불가 혹은 토큰 없음)")
                //토큰이 없으면 회원가입진행
            }
            else -> {
                Log.d(TAG, "KakaoSDK: tokenErrorHandler() - called( 에러 발생 )")
            }
        }
    }

    //무효한 토큰 (정보를 볼 수 없음)
    private fun isInvalidToken(error :Throwable?) :Boolean{
        return error is KakaoSdkError && error.isInvalidTokenError()
    }

    //회원 탈퇴
    fun kakaoWithdraw(){
        userApiClient.unlink { error ->
            error?.let { Log.d(TAG, "KakaoSDK: unlink() - 연결 끊기 실패. SDK에서 토큰 삭제됨 error : ${error}") }
                ?: run { Log.d(TAG, "KakaoSDK: unlink() - 연결 끊기 성공. SDK에서 토큰 삭제됨") }
        }
    }

    // 사용자가 카카오톡 설치 후 디바이스 권한 요청 화면에서 로그인을 취소한 경우,
    // 의도적인 로그인 취소로 보고 카카오계정으로 로그인 시도 없이 로그인 취소로 처리 (예: 뒤로 가기)
    private fun isClientCancel(error : Throwable?) :Boolean{
        return error is ClientError && error.reason == ClientErrorCause.Cancelled
    }

    //카카오톡에 카카오계정이 로그인 되어있는지?
    private fun isNotExistKakaoTalkAcount(error : Throwable?) :Boolean{
        return error is AuthError && error.reason == AuthErrorCause.Unknown
    }

    //사용자가 권한 동의하지 않음
    private fun isClientAccessDenied(error : Throwable?) :Boolean{
        return error is AuthError && error.reason == AuthErrorCause.AccessDenied
    }
}