package com.example.bookchat.ui.mypage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookchat.domain.model.User
import com.example.bookchat.domain.repository.ClientRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyPageViewModel @Inject constructor(
	private val clientRepository: ClientRepository,
) : ViewModel() {
	private val _clientFlow = MutableStateFlow<User>(User.Default)
	val clientFlow get() = _clientFlow.asStateFlow()

	private val _eventFlow = MutableSharedFlow<MyPageEvent>()
	val eventFlow = _eventFlow.asSharedFlow()

	init {
		observeClientFlow()
	}

	private fun observeClientFlow() = viewModelScope.launch {
		clientRepository.getClientFlow().collect { user -> _clientFlow.emit(user) }
	}

	fun onClickUserEditBtn() {
		startEvent(MyPageEvent.MoveToUserEditPage)
	}

	fun onClickWishBtn() {
		startEvent(MyPageEvent.MoveToWish)
	}

	fun onClickNoticeBtn() {
		startEvent(MyPageEvent.MoveToNotice)
	}

	fun onClickAccountSetBtn() {
		startEvent(MyPageEvent.MoveToAccountSetting)
	}

	fun onClickAppSetBtn() {
		startEvent(MyPageEvent.MoveToAppSetting)
	}

	private fun startEvent(event: MyPageEvent) = viewModelScope.launch {
		_eventFlow.emit(event)
	}

	sealed class MyPageEvent {
		object MoveToUserEditPage : MyPageEvent()
		object MoveToWish : MyPageEvent()
		object MoveToNotice : MyPageEvent()
		object MoveToAccountSetting : MyPageEvent()
		object MoveToAppSetting : MyPageEvent()
	}
}