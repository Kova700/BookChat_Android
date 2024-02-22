package com.example.bookchat.ui.viewmodel

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookchat.data.response.ForbiddenException
import com.example.bookchat.data.response.KakaoLoginFailException
import com.example.bookchat.data.response.KakaoLoginUserCancelException
import com.example.bookchat.data.response.NeedToDeviceWarningException
import com.example.bookchat.data.response.NeedToSignUpException
import com.example.bookchat.data.response.NetworkIsNotConnectedException
import com.example.bookchat.domain.repository.UserRepository
import com.example.bookchat.oauth.GoogleSDK
import com.example.bookchat.oauth.KakaoSDK
import com.example.bookchat.utils.Constants.TAG
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

	val uiStateFlow = MutableStateFlow<LoginUiState>(LoginUiState.Default)

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
			.also { setUiStateToDefault() }
	}

	fun bookchatLogin(approveChangingDevice: Boolean = false) = viewModelScope.launch {
		Log.d(TAG, "LoginViewModel: bookchatLogin() - called")
		setUiStateToLoading()
		runCatching { userRepository.signIn(approveChangingDevice) }
			.onSuccess { requestUserInfo() }
			.onFailure { failHandler(it) }
			.also { setUiStateToDefault() }
	}

	private fun startEvent(event: LoginEvent) = viewModelScope.launch {
		_eventFlow.emit(event)
	}

	fun clickKakaoLoginBtn(context: Context) {
		if (!isUiStateDefault()) return
		setUiStateToLoading()
		startKakaoLogin(context)
	}

	fun clickGoogleLoginBtn(context: Context, resultLauncher: ActivityResultLauncher<Intent>) {
		if (!isUiStateDefault()) return
		setUiStateToLoading()
		resultLauncher.launch(GoogleSDK.getSignInIntent(context))
	}

	sealed class LoginEvent {
		object MoveToMain : LoginEvent()
		object MoveToSignUp : LoginEvent()
		object ShowDeviceWarning : LoginEvent()
		object ForbiddenUser : LoginEvent()
		object NetworkError : LoginEvent()
		object KakaoLoginFail : LoginEvent()
		object UnknownError : LoginEvent()
	}

	sealed class LoginUiState {
		object Loading : LoginUiState()
		object Default : LoginUiState()
	}

	private fun isUiStateDefault() =
		uiStateFlow.value == LoginUiState.Default

	private fun setUiStateToLoading() {
		uiStateFlow.value = LoginUiState.Loading
	}

	fun setUiStateToDefault() {
		uiStateFlow.value = LoginUiState.Default
	}

	private fun failHandler(exception: Throwable) {
		Log.d(TAG, "LoginViewModel: failHandler() - exception : ${exception.message}")
		when (exception) {
			is NeedToSignUpException -> startEvent(LoginEvent.MoveToSignUp)
			is ForbiddenException -> startEvent(LoginEvent.ForbiddenUser)
			is NetworkIsNotConnectedException -> startEvent(LoginEvent.NetworkError)
			is KakaoLoginFailException -> startEvent(LoginEvent.KakaoLoginFail)
			is NeedToDeviceWarningException -> startEvent(LoginEvent.ShowDeviceWarning)
			is KakaoLoginUserCancelException -> {}
			else -> startEvent(LoginEvent.UnknownError)
		}
	}

}