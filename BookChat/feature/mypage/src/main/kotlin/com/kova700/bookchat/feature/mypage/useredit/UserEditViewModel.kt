package com.kova700.bookchat.feature.mypage.useredit

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kova700.bookchat.core.data.client.external.ClientRepository
import com.kova700.bookchat.core.design_system.R
import com.kova700.bookchat.feature.mypage.useredit.UserEditUiState.UiState
import com.kova700.bookchat.util.image.bitmap.compressToByteArray
import com.kova700.bookchat.util.image.bitmap.getImageBitmap
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

	//TODO : 닉네임만 변경할 때, 이미지 Profile은 null로 전송됨
	// null로 전송되면 서버입장에서는 default 이미지로 변경되는지 알고 기존에 이미지를 null로 수정시킴
	// 기본 이미지로 변경된다는 flag를 추가하면 기존 프로필을 변경한다라는 구조로 바꾸는게 더 효율적인거 같은데
	// isProfileChanged = true라면 서버는 새로 받은 이미지로 덮어쓰기(null Or MultiPart)
	// isProfileChanged = false라면 서버는 기존 이미지로 유지
	private fun verifyNickname() {
		val nickName = uiState.value.newNickname
		val userProfile = uiState.value.clientNewImage

		when {
			uiState.value.isNeedDuplicatesNicknameCheck -> checkNicknameDuplication(nickName)
			else -> changeClientProfile(nickName, userProfile)
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

	//TODO : userProfile = null로 보내면 null로 설정이 안됨 (서버 수정 대기중)
	private fun changeClientProfile(
		newNickName: String,
		userProfile: Bitmap?,
	) {
		if (uiState.value.uiState == UiState.LOADING) return
		updateState { copy(uiState = UiState.LOADING) }

		viewModelScope.launch {
			runCatching {
				clientRepository.changeClientProfile(
					newNickname = newNickName,
					userProfile = userProfile?.compressToByteArray()
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