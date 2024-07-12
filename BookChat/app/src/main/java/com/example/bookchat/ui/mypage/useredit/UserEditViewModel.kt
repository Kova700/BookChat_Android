package com.example.bookchat.ui.mypage.useredit

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookchat.R
import com.example.bookchat.data.network.model.response.ForbiddenException
import com.example.bookchat.data.network.model.response.NetworkIsNotConnectedException
import com.example.bookchat.domain.model.NicknameCheckState
import com.example.bookchat.domain.repository.ClientRepository
import com.example.bookchat.utils.Constants.TAG
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

//TODO : 유저 프로필 기본 프로필로 변경하는 DIalog와 같은 UI가 필요함

@HiltViewModel
class UserEditViewModel @Inject constructor(
	private val clientRepository: ClientRepository,
) : ViewModel() {
	private val _eventFlow = MutableSharedFlow<UserEditUiEvent>()
	val eventFlow = _eventFlow.asSharedFlow()

	private val _uiState = MutableStateFlow<UserEditUiState>(UserEditUiState.DEFAULT)
	val uiState = _uiState.asStateFlow()

	init {
		initUiState()
	}

	private fun initUiState() = viewModelScope.launch {
		updateState {
			copy(
				client = clientRepository.getClientProfile(),
				newNickname = clientRepository.getClientProfile().nickname
			)
		}
	}

	//TODO : 사용할 수 없는 이름이거나 중복된 경우 UI노출이 나와야하는데 나오지 않고 있음
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
			.onFailure { handleError(it) }
	}

	fun onClickSubmitBtn() {
		if (uiState.value.uiState == UserEditUiState.UiState.LOADING
			|| uiState.value.nicknameCheckState == NicknameCheckState.IsShort
			|| uiState.value.newNickname.length < 2
		) return

		val nickName = uiState.value.newNickname
		val userProfile = uiState.value.clientNewImage

		if (uiState.value.isNeedDuplicatesNicknameCheck) {
			checkNicknameDuplication(nickName)
			return
		}

		changeClientProfile(nickName, userProfile)
	}

	//TODO : userProfile = null로 보내면 null로 설정이 안됨 (서버 수정 대기중)
	private fun changeClientProfile(
		newNickName: String,
		userProfile: ByteArray?,
	) = viewModelScope.launch {
		updateState { copy(uiState = UserEditUiState.UiState.LOADING) }

		runCatching {
			clientRepository.changeClientProfile(
				newNickname = newNickName,
				userProfile = userProfile
			)
		}
			.onSuccess { newClient ->
				updateState {
					copy(
						client = newClient,
						clientNewImage = null,
						uiState = UserEditUiState.UiState.SUCCESS
					)
				}
				startEvent(UserEditUiEvent.MoveToBack)
			}
			.onFailure {
				handleError(it)
				startEvent(UserEditUiEvent.ErrorEvent(R.string.my_page_profile_edit_fail))
				updateState { copy(uiState = UserEditUiState.UiState.ERROR) }
			}
	}

	fun onEnteredSpecialChar() {
		updateState { copy(nicknameCheckState = NicknameCheckState.IsSpecialCharInText) }
	}

	fun onChangeUserProfile(profile: ByteArray) {
		updateState { copy(clientNewImage = profile) }
	}

	fun onClickBackBtn() {
		startEvent(UserEditUiEvent.MoveToBack)
	}

	fun onChangeNickname(text: String) {
		updateUserNicknameIfValid(text.trim())
	}

	fun onClickCameraBtn() {
		startEvent(UserEditUiEvent.ShowUserProfileEditDialog)
	}

	fun onSelectGallery() {
		startEvent(UserEditUiEvent.MoveToGallery)
	}

	fun onSelectDefaultProfileImage() {
		updateState {
			copy(
				client = client.copy(profileImageUrl = null),
				clientNewImage = null,
				isSelectedDefaultImage = true
			)
		}
	}

	private fun updateUserNicknameIfValid(text: String) {
		if (text.length < 2) {
			updateState {
				copy(
					newNickname = text,
					nicknameCheckState = NicknameCheckState.IsShort
				)
			}
			return
		}

		updateState {
			copy(
				newNickname = text,
				nicknameCheckState = NicknameCheckState.Default
			)
		}
	}

	fun onClickClearNickNameBtn() {
		updateState { copy(newNickname = "") }
	}

	private inline fun updateState(block: UserEditUiState.() -> UserEditUiState) {
		_uiState.update { _uiState.value.block() }
	}

	private fun startEvent(event: UserEditUiEvent) = viewModelScope.launch {
		_eventFlow.emit(event)
	}

	private fun handleError(exception: Throwable) {
		Log.d(TAG, "UserEditViewModel: handleError() - exception :$exception")
		when (exception) {
			is ForbiddenException -> startEvent(UserEditUiEvent.ErrorEvent(R.string.login_forbidden_user))
			is NetworkIsNotConnectedException -> startEvent(UserEditUiEvent.ErrorEvent(R.string.error_network_not_connected))
			else -> {
				val errorMessage = exception.message
				if (errorMessage.isNullOrBlank()) startEvent(UserEditUiEvent.ErrorEvent(R.string.error_else))
				else startEvent(UserEditUiEvent.UnknownErrorEvent(errorMessage))
			}
		}
	}
}