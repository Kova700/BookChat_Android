package com.example.bookchat.ui.mypage.accountsetting

sealed class AccountSettingUiEvent {
	object MoveToLoginPage : AccountSettingUiEvent()
	object ShowWithdrawWarningDialog : AccountSettingUiEvent()
	data class MakeToast(
		val stringId: Int,
	) : AccountSettingUiEvent()
}