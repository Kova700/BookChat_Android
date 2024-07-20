package com.example.bookchat.ui.mypage.accountsetting

sealed class AccountSettingUiEvent {
	object MoveToLoginPage : AccountSettingUiEvent()
	object ShowWithdrawWarningDialog : AccountSettingUiEvent()
	object StartOAuthLogout : AccountSettingUiEvent()
	object StartOAuthWithdraw : AccountSettingUiEvent()
	data class MakeToast(
		val stringId: Int,
	) : AccountSettingUiEvent()
}