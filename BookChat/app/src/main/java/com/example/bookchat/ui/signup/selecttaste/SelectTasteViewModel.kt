package com.example.bookchat.ui.signup.selecttaste

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookchat.R
import com.example.bookchat.data.network.model.response.ForbiddenException
import com.example.bookchat.data.network.model.response.NetworkIsNotConnectedException
import com.example.bookchat.domain.model.ReadingTaste
import com.example.bookchat.domain.repository.ClientRepository
import com.example.bookchat.domain.usecase.LoginUseCase
import com.example.bookchat.ui.signup.SignUpActivity.Companion.EXTRA_SIGNUP_USER_NICKNAME
import com.example.bookchat.ui.signup.SignUpActivity.Companion.EXTRA_USER_PROFILE_BYTE_ARRAY
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
) : ViewModel() {
	private val userNickname = savedStateHandle.get<String>(EXTRA_SIGNUP_USER_NICKNAME)!!
	private val userProfile = savedStateHandle.get<ByteArray?>(EXTRA_USER_PROFILE_BYTE_ARRAY)!!

	private val _eventFlow = MutableSharedFlow<SelectTasteEvent>()
	val eventFlow = _eventFlow.asSharedFlow()

	private val _uiState = MutableStateFlow<SelectTasteState>(SelectTasteState.DEFAULT)
	val uiState get() = _uiState.asStateFlow()

	/** UiState변경 시 깜빡임 때문에 따로 분리 */
	private val _userProfileImage = MutableStateFlow<ByteArray?>(null)
	val userProfileImage get() = _userProfileImage.asStateFlow()

	// TODO : 서버 수정 대기 중
	//  4.독서 취향 누르고 시작하기 누르면 토큰은 넘어오는데 사용자를 확일 할 수없습니다 아래에 스낵바 뜸
	//      {"errorCode":"4040100","message":"사용자를 찾을 수 없습니다."}
	//      다시 시작하기 누르면 그냥 에러 던짐
	//  6.이미지 없이 보내면 {"errorCode":"5000001","message":"이미지 업로드에 실패했습니다."} 넘어옴 (전송부 확인해볼 것)
	init {
		initUiState()
	}

	private fun initUiState() {
		updateState { copy(nickname = userNickname) }
		_userProfileImage.value = if (userProfile.isEmpty()) null else userProfile

	}

	private fun signUp() = viewModelScope.launch {
		runCatching {
			clientRepository.signUp(
				nickname = uiState.value.nickname,
				readingTastes = uiState.value.readingTastes,
				userProfile = userProfileImage.value
			)
		}.onSuccess { signIn() }
			.onFailure { failHandler(it) }
	}

	private fun signIn() = viewModelScope.launch {
		runCatching { loginUseCase() }
			.onSuccess { getClientProfile() }
			.onFailure { failHandler(it) }
	}

	private fun getClientProfile() = viewModelScope.launch {
		runCatching { clientRepository.getClientProfile() }
			.onSuccess { startEvent(SelectTasteEvent.MoveToMain) }
			.onFailure { failHandler(it) }
	}

	fun onClickSignUpBtn() {
		signUp()
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

	private fun failHandler(exception: Throwable) {
		when (exception) {
			is ForbiddenException -> startEvent(SelectTasteEvent.ErrorEvent(R.string.login_forbidden_user))
			is NetworkIsNotConnectedException -> startEvent(SelectTasteEvent.ErrorEvent(R.string.error_network_not_connected))
			else -> {
				val errorMessage = exception.message
				if (errorMessage.isNullOrBlank()) startEvent(SelectTasteEvent.ErrorEvent(R.string.error_else))
				else startEvent(SelectTasteEvent.UnknownErrorEvent(errorMessage))
			}
		}
	}
}