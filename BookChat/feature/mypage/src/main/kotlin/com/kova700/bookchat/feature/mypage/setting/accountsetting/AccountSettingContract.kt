package com.kova700.bookchat.feature.mypage.setting.accountsetting

data class AccountSettingUiState(
	val uiState: UiState,
) {
	val isLoading
		get() = uiState == UiState.LOADING

	enum class UiState {
		SUCCESS,
		LOADING,
		ERROR,
	}

	companion object {
		val DEFAULT = AccountSettingUiState(
			uiState = UiState.SUCCESS,
		)
	}
}

sealed class AccountSettingUiEvent {
	data object MoveToLoginPage : AccountSettingUiEvent()
	data object ShowWithdrawWarningDialog : AccountSettingUiEvent()
	data object StartOAuthLogout : AccountSettingUiEvent()
	data object StartOAuthWithdraw : AccountSettingUiEvent()
	data class ShowSnackBar(
		val stringId: Int,
	) : AccountSettingUiEvent()
}