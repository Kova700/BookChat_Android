package com.kova700.bookchat.feature.signup.selecttaste

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kova700.bookchat.core.data.client.external.ClientRepository
import com.kova700.bookchat.core.data.client.external.model.AlreadySignedUpException
import com.kova700.bookchat.core.data.client.external.model.ReadingTaste
import com.kova700.bookchat.core.design_system.R
import com.kova700.bookchat.feature.signup.selecttaste.SelectTasteActivity.Companion.EXTRA_SIGNUP_USER_NICKNAME
import com.kova700.bookchat.feature.signup.selecttaste.SelectTasteState.UiState
import com.kova700.core.domain.usecase.client.LoginUseCase
import com.kova700.core.domain.usecase.client.SignUpUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SelectTasteViewModel @Inject constructor(
	private val savedStateHandle: SavedStateHandle,
	private val clientRepository: ClientRepository,
	private val loginUseCase: LoginUseCase,
	private val signUpUseCase: SignUpUseCase,
) : ViewModel() {
	private val userNickname = savedStateHandle.get<String>(EXTRA_SIGNUP_USER_NICKNAME)!!

	private val _eventFlow = MutableSharedFlow<SelectTasteEvent>()
	val eventFlow = _eventFlow.asSharedFlow()

	private val _uiState = MutableStateFlow<SelectTasteState>(SelectTasteState.DEFAULT)
	val uiState get() = _uiState.asStateFlow()

	init {
		initUiState()
	}

	private fun initUiState() {
		updateState { copy(nickname = userNickname) }
	}

	private fun signUp() = viewModelScope.launch {
		if (uiState.value.isLoading) return@launch
		updateState { copy(uiState = UiState.LOADING) }
		runCatching {
			signUpUseCase(
				nickname = uiState.value.nickname,
				readingTastes = uiState.value.readingTastes,
				userProfile = uiState.value.userProfile
			)
		}.onSuccess { signIn() }
			.onFailure { throwable ->
				updateState { copy(uiState = UiState.ERROR) }
				if (throwable is AlreadySignedUpException) startEvent(SelectTasteEvent.ErrorEvent(R.string.sign_up_already_user))
				else startEvent(SelectTasteEvent.ErrorEvent(R.string.sign_up_fail))
			}
	}

	private fun signIn() = viewModelScope.launch {
		runCatching { loginUseCase() }
			.onSuccess { getClientProfile() }
			.onFailure {
				updateState { copy(uiState = UiState.ERROR) }
				startEvent(SelectTasteEvent.ErrorEvent(R.string.sign_in_fail))
			}
	}

	private fun getClientProfile() = viewModelScope.launch {
		runCatching { clientRepository.getClientProfile() }
			.onSuccess {
				updateState { copy(uiState = UiState.SUCCESS) }
				startEvent(SelectTasteEvent.MoveToMain)
			}.onFailure {
				updateState { copy(uiState = UiState.ERROR) }
				startEvent(SelectTasteEvent.ErrorEvent(R.string.get_client_profile_fail))
			}
	}

	fun onClickSignUpBtn() {
		signUp()
	}

	fun onUpdatedUserProfileImage(byteArray: ByteArray?) {
		updateState { copy(userProfile = byteArray) }
	}

	fun onClickTasteBtn(pickedReadingTaste: ReadingTaste) {
		val existingList = uiState.value.readingTastes
		updateState {
			copy(
				readingTastes =
				if (existingList.contains(pickedReadingTaste)) (existingList - pickedReadingTaste)
				else (existingList + pickedReadingTaste)
			)
		}
	}

	fun onClickBackBtn() {
		startEvent(SelectTasteEvent.MoveToBack)
	}

	private inline fun updateState(block: SelectTasteState.() -> SelectTasteState) {
		_uiState.update { _uiState.value.block() }
	}

	private fun startEvent(event: SelectTasteEvent) = viewModelScope.launch {
		_eventFlow.emit(event)
	}
}