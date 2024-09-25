package com.kova700.bookchat.feature.signup.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kova700.bookchat.core.data.client.external.ClientRepository
import com.kova700.bookchat.core.data.common.model.network.ForbiddenException
import com.kova700.bookchat.core.design_system.R
import com.kova700.bookchat.util.user.namecheck.NicknameCheckState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
	private var clientRepository: ClientRepository,
) : ViewModel() {

	private val _eventFlow = MutableSharedFlow<SignUpEvent>()
	val eventFlow = _eventFlow.asSharedFlow()

	private val _uiState = MutableStateFlow<SignUpState>(SignUpState.DEFAULT)
	val uiState = _uiState.asStateFlow()

	private fun verifyNickname() = viewModelScope.launch {
		val nickName = uiState.value.nickname
		val userProfile = uiState.value.clientNewImageUri
		val nameCheckStatus = uiState.value.nicknameCheckState

		when {
			nameCheckStatus != NicknameCheckState.IsPerfect -> checkNicknameDuplication(nickName)
			nameCheckStatus == NicknameCheckState.IsPerfect -> startEvent(
				SignUpEvent.MoveToSelectTaste(
					userNickname = nickName,
					userProfileUri = userProfile
				)
			)
		}
	}

	private fun checkNicknameDuplication(nickName: String) = viewModelScope.launch {
		runCatching { clientRepository.isDuplicatedUserNickName(nickName) }
			.onSuccess { isDuplicated ->
				updateState {
					copy(
						nicknameCheckState =
						if (isDuplicated) NicknameCheckState.IsDuplicate else NicknameCheckState.IsPerfect,
					)
				}
			}
			.onFailure { failHandler(it) }
	}

	fun onClickStartBtn() {
		if (uiState.value.uiState == SignUpState.UiState.LOADING
			|| uiState.value.nicknameCheckState == NicknameCheckState.IsShort
			|| uiState.value.nickname.length < 2
		) return
		verifyNickname()
	}

	fun onClickCameraBtn() {
		startEvent(SignUpEvent.PermissionCheck)
	}

	fun onChangeNickname(text: String) {
		updateUserNicknameIfValid(text.trim())
	}

	private fun updateUserNicknameIfValid(text: String) {

		if (text.length < 2) {
			updateState {
				copy(
					nickname = text,
					nicknameCheckState = NicknameCheckState.IsShort
				)
			}
			return
		}

		updateState {
			copy(
				nickname = text,
				nicknameCheckState = NicknameCheckState.Default
			)
		}
	}

	fun onEnteredSpecialChar() {
		updateState { copy(nicknameCheckState = NicknameCheckState.IsSpecialCharInText) }
	}

	fun onChangeUserProfile(profileUri: String) {
		updateState { copy(clientNewImageUri = profileUri) }
	}

	fun onClickBackBtn() {
		startEvent(SignUpEvent.MoveToBack)
	}

	fun onClickClearNickNameBtn() {
		updateState { copy(nickname = "") }
	}

	private inline fun updateState(block: SignUpState.() -> SignUpState) {
		_uiState.update { _uiState.value.block() }
	}

	private fun startEvent(event: SignUpEvent) = viewModelScope.launch {
		_eventFlow.emit(event)
	}

	private fun failHandler(exception: Throwable) {
		when (exception) {
			is ForbiddenException -> startEvent(SignUpEvent.ErrorEvent(R.string.login_forbidden_user))
			else -> {
				val errorMessage = exception.message
				if (errorMessage.isNullOrBlank()) startEvent(SignUpEvent.ErrorEvent(R.string.error_else))
				else startEvent(SignUpEvent.UnknownErrorEvent(errorMessage))
			}
		}
	}

}