package com.example.bookchat.ui.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookchat.R
import com.example.bookchat.data.network.model.response.ForbiddenException
import com.example.bookchat.data.network.model.response.NeedToDeviceWarningException
import com.example.bookchat.data.network.model.response.NeedToSignUpException
import com.example.bookchat.data.network.model.response.NetworkIsNotConnectedException
import com.example.bookchat.domain.repository.ClientRepository
import com.example.bookchat.domain.repository.OAuthIdTokenRepository
import com.example.bookchat.domain.usecase.LoginUseCase
import com.example.bookchat.oauth.external.model.IdToken
import com.example.bookchat.oauth.external.exception.ClientCancelException
import com.example.bookchat.ui.login.LoginUiState.UiState
import com.example.bookchat.utils.Constants.TAG
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
	private val clientRepository: ClientRepository,
	private val oAuthIdTokenRepository: OAuthIdTokenRepository,
	private val loginUseCase: LoginUseCase,
) : ViewModel() {

	private val _eventFlow = MutableSharedFlow<LoginEvent>()
	val eventFlow = _eventFlow.asSharedFlow()

	private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.DEFAULT)
	val uiState get() = _uiState.asStateFlow()

	private fun login(isDeviceChangeApproved: Boolean = false) = viewModelScope.launch {
		updateState { copy(uiState = UiState.LOADING) }
		runCatching { loginUseCase(isDeviceChangeApproved) }
			.onSuccess { getClientProfile() }
			.onFailure { failHandler(it) }
			.also { updateState { copy(uiState = UiState.SUCCESS) } }
	}

	private fun getClientProfile() = viewModelScope.launch {
		runCatching { clientRepository.getClientProfile() }
			.onSuccess { startEvent(LoginEvent.MoveToMain) }
			.onFailure { failHandler(it) }
	}

	fun onChangeIdToken(idToken: IdToken) {
		updateState { copy(idToken = idToken) }
		oAuthIdTokenRepository.saveIdToken(idToken)
		login()
	}

	fun onFailKakaoLogin(throwable: Throwable) {
		if (throwable is ClientCancelException) return
		startEvent(LoginEvent.ErrorEvent(R.string.error_kakao_login))
	}

	fun onFailGoogleLogin(throwable: Throwable) {
		if (throwable is ClientCancelException) return
		startEvent(LoginEvent.ErrorEvent(R.string.error_google_login))
	}

	fun onClickKakaoLoginBtn() {
		startEvent(LoginEvent.StartKakaoLogin)
	}

	fun onClickGoogleLoginBtn() {
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
			is NetworkIsNotConnectedException -> startEvent(LoginEvent.ErrorEvent(R.string.error_network_not_connected))
			else -> {
				val errorMessage = exception.message
				if (errorMessage.isNullOrBlank()) startEvent(LoginEvent.ErrorEvent(R.string.error_else))
				else startEvent(LoginEvent.UnknownErrorEvent(errorMessage))
			}
		}
	}

}