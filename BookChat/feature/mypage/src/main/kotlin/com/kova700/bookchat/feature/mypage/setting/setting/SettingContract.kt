package com.kova700.bookchat.feature.mypage.setting.setting

sealed class SettingUiEvent {
	data object MoveToAppSetting : SettingUiEvent()
	data object MoveToAccountSetting : SettingUiEvent()
}