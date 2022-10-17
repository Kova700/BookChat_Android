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
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class LoginViewModel(private val userRepository : UserRepository) : ViewModel(){
    private val _eventFlow = MutableSharedFlow<LoginEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    private var recursiveChecker = false //임시 (구조 개선 필요)

    //onFailure에 빠트린 예외처리는 없는지 다른 예외가 나올 수도 있는데 해당 예외만 처리되게 되어있는지 다시 확인해볼 것

    init {
        viewModelScope.launch {
            runCatching{ DataStoreManager.getBookchatToken() }
                .onSuccess { requestUserInfo() }
        }
    }

    private fun requestUserInfo() = viewModelScope.launch {
        Log.d(TAG, "LoginViewModel: requestUserInfo() - called")
        runCatching{ userRepository.getUserProfile() }
            .onSuccess { startEvent(LoginEvent.MoveToMain) }
            .onFailure { failHandler(it) }
    }

    private fun requestTokenRenewal() = viewModelScope.launch {
        Log.d(TAG, "LoginViewModel: requestTokenRenewal() - called")
        runCatching{ userRepository.requestTokenRenewal() }
            .onSuccess { if (recursiveChecker == false) requestUserInfo(); recursiveChecker = true }
            .onFailure { failHandler(it) }
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
                KakaoSDK.kakaoLoginWithKakaoAccount(context) //이거 실패 예외처리 해야함
            }
    }

    fun bookchatLogin() = viewModelScope.launch {
        runCatching{ userRepository.signIn() }
            .onSuccess { requestUserInfo() }
            .onFailure { failHandler(it) }
    }

    fun startGoogleLogin()= viewModelScope.launch {
        Log.d(TAG, "LoginViewModel: requestGoogleLogin() - called")
    }

    private fun startEvent (event :LoginEvent) = viewModelScope.launch {
        _eventFlow.emit(event)
    }

    sealed class LoginEvent {
        object MoveToMain :LoginEvent()
        object MoveToSignUp :LoginEvent()
        object Forbidden :LoginEvent()
        object NetworkError :LoginEvent()
        object UnknownError :LoginEvent()
    }

    private fun failHandler(exception: Throwable) {
        Log.d(TAG, "LoginViewModel: failHandler() - exception : $exception")
        when(exception){
            is TokenExpiredException -> requestTokenRenewal()
            is NeedToSignUpException -> startEvent(LoginEvent.MoveToSignUp)
            is ForbiddenException -> startEvent(LoginEvent.Forbidden)
            is NetworkIsNotConnectedException -> startEvent(LoginEvent.NetworkError)
            else -> startEvent(LoginEvent.UnknownError)
        }
    }

}