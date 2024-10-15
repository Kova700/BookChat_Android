package com.kova700.bookchat.feature.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kova700.bookchat.core.data.client.external.ClientRepository
import com.kova700.bookchat.core.data.client.external.model.NeedToDeviceWarningException
import com.kova700.bookchat.core.data.client.external.model.NeedToSignUpException
import com.kova700.bookchat.core.design_system.R
import com.kova700.bookchat.core.oauth.external.exception.ClientCancelException
import com.kova700.bookchat.feature.login.LoginUiState.UiState
import com.kova700.core.domain.usecase.client.LoginUseCase
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
	private val loginUseCase: LoginUseCase,
) : ViewModel() {

	private val _eventFlow = MutableSharedFlow<LoginEvent>()
	val eventFlow = _eventFlow.asSharedFlow()

	private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.DEFAULT)
	val uiState get() = _uiState.asStateFlow()

	private fun login(isDeviceChangeApproved: Boolean = false) = viewModelScope.launch {
		runCatching { loginUseCase(isDeviceChangeApproved) }
			.onSuccess { getClientProfile() }
			.onFailure { throwable ->
				when (throwable) {
					is NeedToSignUpException -> {
						updateState { copy(uiState = UiState.SUCCESS) }
						startEvent(LoginEvent.MoveToSignUp)
					}

					is NeedToDeviceWarningException -> {
						updateState { copy(uiState = UiState.SUCCESS) }
						startEvent(LoginEvent.ShowDeviceWarning)
					}

					else -> {
						updateState { copy(uiState = UiState.ERROR) }
						startEvent(LoginEvent.ErrorEvent(R.string.sign_in_fail))
					}
				}
			}
	}

	private fun getClientProfile() = viewModelScope.launch {
		runCatching { clientRepository.getClientProfile() }
			.onSuccess {
				updateState { copy(uiState = UiState.SUCCESS) }
				startEvent(LoginEvent.MoveToMain)
			}.onFailure {
				updateState { copy(uiState = UiState.ERROR) }
				startEvent(LoginEvent.ErrorEvent(R.string.get_client_profile_fail))
			}
	}

	fun onChangeIdToken() {
		login()
	}

	fun onFailedKakaoLogin(throwable: Throwable) {
		when (throwable) {
			is ClientCancelException -> updateState { copy(uiState = UiState.SUCCESS) }
			else -> {
				updateState { copy(uiState = UiState.ERROR) }
				startEvent(LoginEvent.ErrorEvent(R.string.error_kakao_login))
			}
		}
	}

	fun onFailedGoogleLogin(throwable: Throwable) {
		when (throwable) {
			is ClientCancelException -> updateState { copy(uiState = UiState.SUCCESS) }
			else -> {
				updateState { copy(uiState = UiState.ERROR) }
				startEvent(LoginEvent.ErrorEvent(R.string.error_google_login))
			}
		}
	}

	fun onClickKakaoLoginBtn() {
		if (uiState.value.isLoading) return
		updateState { copy(uiState = UiState.LOADING) }
		startEvent(LoginEvent.StartKakaoLogin)
	}

	fun onClickGoogleLoginBtn() {
		if (uiState.value.isLoading) return
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

}