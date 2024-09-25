package com.kova700.bookchat.feature.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kova700.bookchat.core.data.client.external.ClientRepository
import com.kova700.bookchat.core.data.client.external.model.NeedToDeviceWarningException
import com.kova700.bookchat.core.data.client.external.model.NeedToSignUpException
import com.kova700.bookchat.core.data.common.model.network.ForbiddenException
import com.kova700.bookchat.core.design_system.R
import com.kova700.bookchat.core.oauth.external.exception.ClientCancelException
import com.kova700.bookchat.feature.login.LoginUiState.UiState
import com.kova700.bookchat.util.Constants.TAG
import com.kova700.core.domain.usecase.client.LoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

//TODO : 로그인 시에 로딩화면 안보임 체크 필요
@HiltViewModel
class LoginViewModel @Inject constructor(
	private val clientRepository: ClientRepository,
	private val loginUseCase: LoginUseCase,
) : ViewModel() {

	private val _eventFlow = MutableSharedFlow<LoginEvent>()
	val eventFlow = _eventFlow.asSharedFlow()

	private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.DEFAULT)
	val uiState get() = _uiState.asStateFlow()

	private fun login(isDeviceChangeApproved: Boolean = false) = viewModelScope.launch {
		runCatching { loginUseCase(isDeviceChangeApproved) }
			.onSuccess { getClientProfile() }
			.onFailure { failHandler(it) }
	}

	private fun getClientProfile() = viewModelScope.launch {
		runCatching { clientRepository.getClientProfile() }
			.onSuccess { startEvent(LoginEvent.MoveToMain) }
			.onFailure { failHandler(it) }
	}

	fun onChangeIdToken() {
		login()
	}

	fun onFailedKakaoLogin(throwable: Throwable) {
		updateState { copy(uiState = UiState.SUCCESS) }
		Log.d(TAG, "LoginViewModel: onFailedKakaoLogin() - throwable : $throwable")
		if (throwable is ClientCancelException) return
		startEvent(LoginEvent.ErrorEvent(R.string.error_kakao_login))
	}

	fun onFailedGoogleLogin(throwable: Throwable) {
		updateState { copy(uiState = UiState.SUCCESS) }
		Log.d(TAG, "LoginViewModel: onFailedGoogleLogin() - throwable : $throwable")
		if (throwable is ClientCancelException) return
		startEvent(LoginEvent.ErrorEvent(R.string.error_google_login))
	}

	fun onClickKakaoLoginBtn() {
		if (uiState.value.uiState == UiState.LOADING) return
		updateState { copy(uiState = UiState.LOADING) }
		startEvent(LoginEvent.StartKakaoLogin)
	}

	fun onClickGoogleLoginBtn() {
		if (uiState.value.uiState == UiState.LOADING) return
		updateState { copy(uiState = UiState.LOADING) }
		startEvent(LoginEvent.StartGoogleLogin)
	}

	fun onClickDeviceWarningOk() {
		login(isDeviceChangeApproved = true)
	}

	private fun startEvent(event: LoginEvent) = viewModelScope.launch {
		_eventFlow.emit(event)
	}

	private inline fun updateState(block: LoginUiState.() -> LoginUiState) {
		_uiState.update { _uiState.value.block() }
	}

	private fun failHandler(exception: Throwable) {
		updateState { copy(uiState = UiState.SUCCESS) }
		Log.d(TAG, "LoginViewModel: failHandler() - exception : $exception")
		when (exception) {
			is NeedToSignUpException -> startEvent(LoginEvent.MoveToSignUp)
			is NeedToDeviceWarningException -> startEvent(LoginEvent.ShowDeviceWarning)
			is ForbiddenException -> startEvent(LoginEvent.ErrorEvent(R.string.login_forbidden_user))
			else -> {
				val errorMessage = exception.message
				if (errorMessage.isNullOrBlank()) startEvent(LoginEvent.ErrorEvent(R.string.error_else))
				else startEvent(LoginEvent.UnknownErrorEvent(errorMessage))
			}
		}
	}

}