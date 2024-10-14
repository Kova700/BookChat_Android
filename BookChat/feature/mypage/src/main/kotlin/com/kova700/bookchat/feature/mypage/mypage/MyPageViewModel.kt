package com.kova700.bookchat.feature.mypage.mypage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kova700.bookchat.core.data.client.external.ClientRepository
import com.kova700.bookchat.core.data.user.external.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyPageViewModel @Inject constructor(
	private val clientRepository: ClientRepository,
) : ViewModel() {
	private val _uiState = MutableStateFlow<User>(User.DEFAULT)
	val uiState get() = _uiState.asStateFlow()

	private val _eventFlow = MutableSharedFlow<MyPageEvent>()
	val eventFlow = _eventFlow.asSharedFlow()

	init {
		observeClientFlow()
	}

	private fun observeClientFlow() = viewModelScope.launch {
		clientRepository.getClientFlow().collect { user -> _uiState.update { user } }
	}

	fun onClickUserEditBtn() {
		startEvent(MyPageEvent.MoveToUserEditPage)
	}

	fun onClickSettingBtn() {
		startEvent(MyPageEvent.MoveToSetting)
	}

	fun onClickNoticeBtn() {
		startEvent(MyPageEvent.MoveToNotice)
	}

	fun onClickOpenSourceLicense() {
		startEvent(MyPageEvent.MoveToOpenSourceLicense)
	}

	fun onClickTermsBtn() {
		startEvent(MyPageEvent.MoveToTerms)
	}

	fun onClickPolicyBtn() {
		startEvent(MyPageEvent.MoveToPrivacyPolicy)
	}

	private fun startEvent(event: MyPageEvent) = viewModelScope.launch {
		_eventFlow.emit(event)
	}
}