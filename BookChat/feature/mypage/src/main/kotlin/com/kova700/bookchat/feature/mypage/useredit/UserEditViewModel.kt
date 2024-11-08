package com.kova700.bookchat.feature.mypage.useredit

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kova700.bookchat.core.data.client.external.ClientRepository
import com.kova700.bookchat.core.design_system.R
import com.kova700.bookchat.feature.mypage.useredit.UserEditUiState.UiState
import com.kova700.bookchat.util.image.bitmap.compressToByteArray
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

	private fun verifyNickname() {
		val nickname = uiState.value.newNickname
		val userProfile = uiState.value.clientNewImage
		val isProfileChanged = uiState.value.isProfileChanged

		when {
			uiState.value.isNeedDuplicatesNicknameCheck -> checkNicknameDuplication(nickname)
			else -> changeClientProfile(
				newNickname = nickname,
				userProfile = userProfile,
				isProfileChanged = isProfileChanged
			)
		}
	}

	private fun checkNicknameDuplication(nickName: String) {
		if (uiState.value.uiState == UiState.LOADING) return
		updateState { copy(uiState = UiState.LOADING) }

		viewModelScope.launch {
			runCatching { clientRepository.isDuplicatedUserNickName(nickName) }
				.onSuccess { isDuplicated ->
					updateState {
						copy(
							uiState = UiState.SUCCESS,
							nicknameCheckState =
							if (isDuplicated) NicknameCheckState.IsDuplicate else NicknameCheckState.IsPerfect,
						)
					}
				}
				.onFailure {
					updateState { copy(uiState = UiState.ERROR) }
					startEvent(UserEditUiEvent.ShowSnackBar(R.string.error_else))
					handleError(it)
				}
		}
	}

	fun onClickSubmitBtn() {
		if (uiState.value.uiState == UiState.LOADING
			|| uiState.value.nicknameCheckState == NicknameCheckState.IsShort
			|| uiState.value.newNickname.length < 2
		) return
		startEvent(UserEditUiEvent.CloseKeyboard)
		verifyNickname()
	}

	private fun changeClientProfile(
		newNickname: String,
		userProfile: Bitmap?,
		isProfileChanged: Boolean
	) {
		if (uiState.value.uiState == UiState.LOADING) return
		updateState { copy(uiState = UiState.LOADING) }

		viewModelScope.launch {
			runCatching {
				clientRepository.changeClientProfile(
					newNickname = newNickname,
					userProfile = userProfile?.compressToByteArray(),
					isProfileChanged = isProfileChanged
				)
			}
				.onSuccess { newClient ->
					updateState {
						copy(
							client = newClient,
							clientNewImage = null,
							uiState = UiState.SUCCESS
						)
					}
					startEvent(UserEditUiEvent.MoveToBack)
				}
				.onFailure {
					handleError(it)
					startEvent(UserEditUiEvent.ShowSnackBar(R.string.my_page_profile_edit_fail))
					updateState { copy(uiState = UiState.ERROR) }
				}
		}
	}

	fun onEnteredSpecialChar() {
		updateState { copy(nicknameCheckState = NicknameCheckState.IsSpecialCharInText) }
	}

	fun onChangeUserProfile(profile: Bitmap) {
		updateState {
			copy(
				clientNewImage = profile,
				isSelectedDefaultImage = false
			)
		}
	}

	fun onClickBackBtn() {
		startEvent(UserEditUiEvent.MoveToBack)
	}

	fun onChangeNickname(text: String) {
		updateUserNicknameIfValid(text.trim())
	}

	fun onClickCameraBtn() {
		startEvent(UserEditUiEvent.ShowProfileEditDialog)
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

	private fun handleError(exception: Throwable) {}
}