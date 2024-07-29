package com.example.bookchat.ui.mypage.setting.accountsetting

sealed class AccountSettingUiEvent {
	data object MoveToLoginPage : AccountSettingUiEvent()
	data object ShowWithdrawWarningDialog : AccountSettingUiEvent()
	data object StartOAuthLogout : AccountSettingUiEvent()
	data object StartOAuthWithdraw : AccountSettingUiEvent()
	data class MakeToast(
		val stringId: Int,
	) : AccountSettingUiEvent()
}