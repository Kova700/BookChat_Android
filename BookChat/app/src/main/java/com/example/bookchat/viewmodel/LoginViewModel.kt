package com.example.bookchat.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookchat.data.response.*
import com.example.bookchat.oauth.KakaoSDK
import com.example.bookchat.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _eventFlow = MutableSharedFlow<LoginEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    private val uiStateFlow = MutableStateFlow<LoginUiState>(LoginUiState.Default)

    private fun requestUserInfo() = viewModelScope.launch {
        runCatching { userRepository.getUserProfile() }
            .onSuccess { startEvent(LoginEvent.MoveToMain) }
            .onFailure { failHandler(it) }
    }

    fun startKakaoLogin(context: Context) = viewModelScope.launch {
        runCatching { KakaoSDK.kakaoLogin(context) }
            .onSuccess { bookchatLogin() }
            .onFailure { failHandler(it) }
            .also { setUiStateToDefault() }
    }

    fun bookchatLogin() = viewModelScope.launch {
        runCatching { userRepository.signIn() }
            .onSuccess { requestUserInfo() }
            .onFailure { failHandler(it) }
            .also { setUiStateToDefault() }
    }

    private fun startEvent(event: LoginEvent) = viewModelScope.launch {
        _eventFlow.emit(event)
    }

    sealed class LoginEvent {
        object MoveToMain : LoginEvent()
        object MoveToSignUp : LoginEvent()
        object ForbiddenUser : LoginEvent()
        object NetworkError : LoginEvent()
        object KakaoLoginFail : LoginEvent()
        object NeedToGoogleLogin : LoginEvent()
        object UnknownError : LoginEvent()
    }

    sealed class LoginUiState {
        object Loading :LoginUiState()
        object Default :LoginUiState()
    }

    fun isUiStateDefault() =
        uiStateFlow.value == LoginUiState.Default

    fun setUiStateToLoading() {
        uiStateFlow.value = LoginUiState.Loading
    }

    fun setUiStateToDefault() {
        uiStateFlow.value = LoginUiState.Default
    }

    private fun failHandler(exception: Throwable) {
        when (exception) {
            is NeedToSignUpException -> startEvent(LoginEvent.MoveToSignUp)
            is ForbiddenException -> startEvent(LoginEvent.ForbiddenUser)
            is NetworkIsNotConnectedException -> startEvent(LoginEvent.NetworkError)
            is KakaoLoginFailException -> startEvent(LoginEvent.KakaoLoginFail)
            is NeedToGoogleLoginException -> startEvent(LoginEvent.NeedToGoogleLogin)
            is KakaoLoginUserCancelException -> {}
            else -> startEvent(LoginEvent.UnknownError)
        }
    }

}