package com.kova700.bookchat.feature.signup.selecttaste

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kova700.bookchat.core.data.client.external.ClientRepository
import com.kova700.bookchat.core.data.client.external.model.ReadingTaste
import com.kova700.bookchat.core.data.common.model.network.ForbiddenException
import com.kova700.bookchat.core.design_system.R
import com.kova700.core.domain.usecase.client.LoginUseCase
import com.kova700.core.domain.usecase.client.SignUpUseCase
import com.kova700.bookchat.feature.signup.selecttaste.SelectTasteActivity.Companion.EXTRA_SIGNUP_USER_NICKNAME
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
	}

	private fun signUp() = viewModelScope.launch {
		runCatching {
			signUpUseCase(
				nickname = uiState.value.nickname,
				readingTastes = uiState.value.readingTastes,
				userProfile = uiState.value.userProfile
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

	private fun failHandler(exception: Throwable) {
		when (exception) {
			is ForbiddenException -> startEvent(SelectTasteEvent.ErrorEvent(R.string.login_forbidden_user))
			else -> {
				val errorMessage = exception.message
				if (errorMessage.isNullOrBlank()) startEvent(SelectTasteEvent.ErrorEvent(R.string.error_else))
				else startEvent(SelectTasteEvent.UnknownErrorEvent(errorMessage))
			}
		}
	}
}