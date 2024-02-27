package com.example.bookchat.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookchat.domain.model.User
import com.example.bookchat.domain.repository.ClientRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyPageViewModel @Inject constructor(
	private val clientRepository: ClientRepository
) : ViewModel() {
	val cachedClient = MutableStateFlow<User>(User.Default)

	init {
		getUserInfo()
	}

	private val _eventFlow = MutableSharedFlow<MyPageEvent>()
	val eventFlow = _eventFlow.asSharedFlow()

	fun clickUserEditBtn() {
		startEvent(MyPageEvent.MoveToUserEditPage)
	}

	fun clickWishBtn() {
		startEvent(MyPageEvent.MoveToWish)
	}

	fun clickNoticeBtn() {
		startEvent(MyPageEvent.MoveToNotice)
	}

	fun clickAccountSetBtn() {
		startEvent(MyPageEvent.MoveToAccountSetting)
	}

	fun clickAppSetBtn() {
		startEvent(MyPageEvent.MoveToAppSetting)
	}

	private fun getUserInfo() = viewModelScope.launch {
		runCatching { clientRepository.getClientProfile() }
			.onSuccess { user -> cachedClient.update { user } }
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