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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor (
    private val userRepository: UserRepository
    ) : ViewModel() {

    private val _eventFlow = MutableSharedFlow<LoginEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    init {
        viewModelScope.launch {
            runCatching { DataStoreManager.getBookchatToken() }
                .onSuccess { requestUserInfo() }
        }
    }

    private fun requestUserInfo() = viewModelScope.launch {
        Log.d(TAG, "LoginViewModel: requestUserInfo() - called")
        runCatching { userRepository.getUserProfile() }
            .onSuccess { startEvent(LoginEvent.MoveToMain) }
            .onFailure { failHandler(it) }
    }

    fun startKakaoLogin(context: Context) = viewModelScope.launch {
        Log.d(TAG, "LoginViewModel: startKakaoLogin() - called")
        runCatching { KakaoSDK.kakaoLogin(context) }
            .onSuccess { bookchatLogin() }
            .onFailure { failHandler(it) }
    }

    fun bookchatLogin() = viewModelScope.launch {
        runCatching { userRepository.signIn() }
            .onSuccess { requestUserInfo() }
            .onFailure { failHandler(it) }
    }

    fun startGoogleLogin() = viewModelScope.launch {
        Log.d(TAG, "LoginViewModel: requestGoogleLogin() - called")
    }

    private fun startEvent(event: LoginEvent) = viewModelScope.launch {
        _eventFlow.emit(event)
    }

    sealed class LoginEvent {
        object MoveToMain : LoginEvent()
        object MoveToSignUp : LoginEvent()
        object Forbidden : LoginEvent()
        object NetworkError : LoginEvent()
        object UnknownError : LoginEvent()
        object KakaoLoginFail : LoginEvent()
    }

    private fun failHandler(exception: Throwable) {
        Log.d(TAG, "LoginViewModel: failHandler() - exception : $exception")
        when (exception) {
            is NeedToSignUpException -> startEvent(LoginEvent.MoveToSignUp)
            is ForbiddenException -> startEvent(LoginEvent.Forbidden)
            is NetworkIsNotConnectedException -> startEvent(LoginEvent.NetworkError)
            is KakaoLoginFailException -> startEvent(LoginEvent.KakaoLoginFail)
            else -> startEvent(LoginEvent.UnknownError)
        }
    }

}