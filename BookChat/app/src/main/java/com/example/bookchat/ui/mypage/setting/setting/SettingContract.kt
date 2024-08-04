package com.example.bookchat.ui.mypage.setting.setting

sealed class SettingUiEvent {
	data object MoveToAppSetting : SettingUiEvent()
	data object MoveToAccountSetting : SettingUiEvent()
}