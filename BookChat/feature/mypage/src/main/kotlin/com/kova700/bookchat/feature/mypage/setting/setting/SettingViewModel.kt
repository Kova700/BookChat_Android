package com.kova700.bookchat.feature.mypage.setting.setting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingViewModel @Inject constructor(
) : ViewModel() {

	private val _eventFlow = MutableSharedFlow<SettingUiEvent>()
	val eventFlow = _eventFlow.asSharedFlow()

	fun onClickAppSettingBtn() {
		startEvent(SettingUiEvent.MoveToAppSetting)
	}

	fun onClickAccountSettingBtn() {
		startEvent(SettingUiEvent.MoveToAccountSetting)
	}

	private fun startEvent(event: SettingUiEvent) = viewModelScope.launch {
		_eventFlow.emit(event)
	}
}