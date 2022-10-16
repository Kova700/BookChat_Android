package com.example.bookchat.viewmodel

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookchat.App
import com.example.bookchat.R
import com.example.bookchat.kakao.KakaoSDK
import com.example.bookchat.repository.UserRepository
import com.example.bookchat.response.*
import com.example.bookchat.utils.Constants.TAG
import com.example.bookchat.utils.DataStoreManager
import kotlinx.coroutines.launch

class LoginViewModel(private val userRepository : UserRepository) : ViewModel(){
    lateinit var goMainActivityCallBack :() -> Unit //토큰 가져왔으면 페이지 이동 실행 (이거 이전해야함 UninitializedPropertyAccessException 나올 수 있음)
    lateinit var goSignUpActivityCallBack :() -> Unit
    private var recursiveChecker = false //임시 (구조 개선 필요)

    //onFailure에 빠트린 예외처리는 없는지 다른 예외가 나올 수도 있는데 해당 예외만 처리되게 되어있는지 다시 확인해볼 것

    init {
        viewModelScope.launch {
            runCatching{ DataStoreManager.getBookchatToken() }
                .onSuccess { requestUserInfo() } //유저 정보 가져오기 성공했는데 콜백을 가져올 LoginActivity가 없는 상황에 위에 에러가 터짐
        }
    }

    private fun requestUserInfo() = viewModelScope.launch {
        Log.d(TAG, "LoginViewModel: requestUserInfo() - called")
        runCatching{ userRepository.getUserProfile() }
            .onSuccess { goMainActivityCallBack() }
            .onFailure { failHandler(it) }
    }

    private fun requestTokenRenewal() = viewModelScope.launch {
        Log.d(TAG, "LoginViewModel: requestTokenRenewal() - called")
        runCatching{ userRepository.requestTokenRenewal() }
            .onSuccess { if (recursiveChecker == false) requestUserInfo(); recursiveChecker = true }
            .onFailure { Log.d(TAG, "LoginViewModel: requestTokenRenewal() - onFailure : $it") }
    }

    //Status Code별 Exception handle
    private fun failHandler(exception: Throwable) {
        when(exception){
            is TokenExpiredException -> requestTokenRenewal()
            is UnauthorizedOrBlockedUserException -> {
                Log.d(TAG, "LoginViewModel: failHandler() - unauthorizedOrBlockedUserException")
            }
            is ResponseBodyEmptyException -> {
                Log.d(TAG, "LoginViewModel: failHandler() - ResponseBodyEmptyException")
            }
            is NetworkIsNotConnectedException -> {
                //추후에 스낵바 혹은 유튜브처럼 구현
                Toast.makeText(App.instance.applicationContext,R.string.message_error_network_toast, Toast.LENGTH_SHORT).show()
            }
            else -> {
                Toast.makeText(App.instance.applicationContext, R.string.message_error_else_toast, Toast.LENGTH_SHORT).show()
                Log.d(TAG, "LoginViewModel: failHandler() - $exception")}
        }
    }

    fun startKakaoLogin(context :Context) = viewModelScope.launch {
        Log.d(TAG, "LoginViewModel: startKakaoLogin() - called")
        runCatching{ KakaoSDK.kakaoLogin(context) }
            .onSuccess { bookchatLogin() }
            .onFailure {
                Log.d(TAG, "LoginViewModel: startKakaoLogin() - onFailure - $it")
                //회원가입 해야함 (회원가입 페이지로 이동)
                //카카오 SDK에서 오류혹은 그외 오류 발생시 사이드 이펙트 확인
                //카카오톡은 깔려있으나 계정연결이 없는 경우 등등
                KakaoSDK.kakaoLoginWithKakaoAccount(context)
            }
    }

    fun bookchatLogin() = viewModelScope.launch {
        runCatching{ userRepository.signIn() }
            .onSuccess {
                Log.d(TAG, "LoginViewModel: bookchatLogin() - onSuccess")
                requestUserInfo()
            }
            .onFailure {
                Log.d(TAG, "LoginViewModel: bookchatLogin() - onFailure $it")
                when(it){
                    is NeedToSignUpException -> goSignUpActivityCallBack()
                    is UnauthorizedOrBlockedUserException -> Toast.makeText(App.instance.applicationContext,R.string.message_blocked_user, Toast.LENGTH_SHORT).show()
                    else -> Log.d(TAG, "LoginViewModel: bookchatLogin() - elseException : $it")
                }

            }
    }

    fun requestGoogleLogin()= viewModelScope.launch {
        Log.d(TAG, "LoginViewModel: requestGoogleLogin() - called")
    }

}