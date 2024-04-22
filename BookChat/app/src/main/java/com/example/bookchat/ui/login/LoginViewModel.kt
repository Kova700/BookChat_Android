package com.example.bookchat.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookchat.R
import com.example.bookchat.data.response.ForbiddenException
import com.example.bookchat.data.response.KakaoLoginFailException
import com.example.bookchat.data.response.NeedToDeviceWarningException
import com.example.bookchat.data.response.NeedToSignUpException
import com.example.bookchat.data.response.NetworkIsNotConnectedException
import com.example.bookchat.domain.model.IdToken
import com.example.bookchat.domain.repository.ClientRepository
import com.example.bookchat.ui.login.LoginUiState.UiState
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
	private val clientRepository: ClientRepository
) : ViewModel() {

	private val _eventFlow = MutableSharedFlow<LoginEvent>()
	val eventFlow = _eventFlow.asSharedFlow()

	private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.DEFAULT)
	val uiState get() = _uiState.asStateFlow()

	fun login(
		isApproveChangingDevice: Boolean = false
	) = viewModelScope.launch {
		updateState { copy(uiState = UiState.LOADING) }
		runCatching { clientRepository.signIn(isApproveChangingDevice) }
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
		clientRepository.saveIdToken(idToken)
	}

	fun onClickKakaoLoginBtn() {
		if (uiState.value.uiState != UiState.SUCCESS) return
		updateState { copy(uiState = UiState.LOADING) }
		startEvent(LoginEvent.StartKakaoLogin)
	}

	fun onClickGoogleLoginBtn() {
		if (uiState.value.uiState != UiState.SUCCESS) return
		updateState { copy(uiState = UiState.LOADING) }
		startEvent(LoginEvent.StartGoogleLogin)
	}

	fun onClickDeviceWarningOk() {
		login(isApproveChangingDevice = true)
	}

	private fun startEvent(event: LoginEvent) = viewModelScope.launch {
		_eventFlow.emit(event)
	}

	private inline fun updateState(block: LoginUiState.() -> LoginUiState) {
		_uiState.update { _uiState.value.block() }
	}

	private fun failHandler(exception: Throwable) {
		updateState { copy(uiState = UiState.SUCCESS) }
		when (exception) {
			is NeedToSignUpException -> startEvent(LoginEvent.MoveToSignUp)
			is NeedToDeviceWarningException -> startEvent(LoginEvent.ShowDeviceWarning)
			is ForbiddenException -> startEvent(LoginEvent.ErrorEvent(R.string.login_forbidden_user))
			is NetworkIsNotConnectedException -> startEvent(LoginEvent.ErrorEvent(R.string.error_network))
			is KakaoLoginFailException -> startEvent(LoginEvent.ErrorEvent(R.string.error_kakao_login))
			else -> {
				val errorMessage = exception.message
				if (errorMessage.isNullOrBlank()) startEvent(LoginEvent.ErrorEvent(R.string.error_else))
				else startEvent(LoginEvent.UnknownErrorEvent(errorMessage))
			}
		}
	}

}