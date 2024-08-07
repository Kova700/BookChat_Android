package com.example.bookchat.ui.mypage.setting.accountsetting

sealed class AccountSettingUiEvent {
	data object MoveToLoginPage : AccountSettingUiEvent()
	data object ShowWithdrawWarningDialog : AccountSettingUiEvent()
	data object StartOAuthLogout : AccountSettingUiEvent()
	data object StartOAuthWithdraw : AccountSettingUiEvent()
	data class ShowSnackBar(
		val stringId: Int,
	) : AccountSettingUiEvent()
}