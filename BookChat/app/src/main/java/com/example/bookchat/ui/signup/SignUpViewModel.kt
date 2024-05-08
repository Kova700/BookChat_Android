package com.example.bookchat.ui.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookchat.R
import com.example.bookchat.data.network.model.response.ForbiddenException
import com.example.bookchat.data.network.model.response.NetworkIsNotConnectedException
import com.example.bookchat.data.network.model.response.NickNameDuplicateException
import com.example.bookchat.domain.model.NameCheckStatus
import com.example.bookchat.domain.repository.ClientRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.regex.Pattern
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
	private var clientRepository: ClientRepository
) : ViewModel() {

	private val _eventFlow = MutableSharedFlow<SignUpEvent>()
	val eventFlow = _eventFlow.asSharedFlow()

	private val _uiState = MutableStateFlow<SignUpState>(SignUpState.DEFAULT)
	val uiState = _uiState.asStateFlow()

	private fun isAvailableNickname(text: String): Boolean {
		if (text.length < 2) {
			updateState {
				copy(
					nickname = text,
					nameCheckStatus = NameCheckStatus.IsShort
				)
			}
			return false
		}

		val regex =
			"^[a-zA-Z0-9가-힣ㄱ-ㅎㅏ-ㅣ\\u318D\\u119E\\u11A2\\u2022\\u2025a\\u00B7\\uFE55\\uFF1A]+$"
		val pattern = Pattern.compile(regex)
		if (pattern.matcher(text).matches().not()) {
			updateState { copy(nameCheckStatus = NameCheckStatus.IsSpecialCharInText) }
			return false
		}

		updateState {
			copy(
				nickname = text,
				nameCheckStatus = NameCheckStatus.Default
			)
		}
		return true
	}

	private fun checkNicknameDuplication(nickName: String) = viewModelScope.launch {
		runCatching { clientRepository.checkForDuplicateUserName(nickName) }
			.onSuccess { updateState { copy(nameCheckStatus = NameCheckStatus.IsPerfect) } }
			.onFailure { failHandler(it) }
	}

	private fun onDuplicateNickname() {
		updateState { copy(nameCheckStatus = NameCheckStatus.IsDuplicate) }
	}

	fun openGallery() {
		startEvent(SignUpEvent.PermissionCheck)
	}

	fun onClickStartBtn() {
		if (uiState.value.uiState == SignUpState.UiState.LOADING) return

		val nickName = uiState.value.nickname
		val userProfile = uiState.value.userProfileImage
		val nameCheckStatus = uiState.value.nameCheckStatus

		if (isAvailableNickname(nickName).not()) return

		if (nameCheckStatus != NameCheckStatus.IsPerfect) {
			checkNicknameDuplication(nickName)
			return
		}

		startEvent(
			SignUpEvent.MoveToSelectTaste(
				userNickname = nickName,
				userProfilByteArray = userProfile
			)
		)
	}

	fun onChangeNickname(text: String) {
		isAvailableNickname(text.trim())
	}

	fun onChangeUserProfile(profile: ByteArray) {
		updateState { copy(userProfileImage = profile) }
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
			is NetworkIsNotConnectedException -> startEvent(SignUpEvent.ErrorEvent(R.string.error_network))
			is NickNameDuplicateException -> onDuplicateNickname()
			else -> {
				val errorMessage = exception.message
				if (errorMessage.isNullOrBlank()) startEvent(SignUpEvent.ErrorEvent(R.string.error_else))
				else startEvent(SignUpEvent.UnknownErrorEvent(errorMessage))
			}
		}
	}
}