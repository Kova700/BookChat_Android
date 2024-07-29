package com.example.bookchat.ui.mypage.setting.accountsetting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookchat.R
import com.example.bookchat.domain.usecase.LogoutUseCase
import com.example.bookchat.domain.usecase.WithdrawUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccountSettingViewModel @Inject constructor(
	private val logoutUseCase: LogoutUseCase,
	private val withdrawUseCase: WithdrawUseCase,
) : ViewModel() {

	private val _eventFlow = MutableSharedFlow<AccountSettingUiEvent>()
	val eventFlow = _eventFlow.asSharedFlow()

	private fun logout() = viewModelScope.launch {
		runCatching { logoutUseCase() }
			.onSuccess { startEvent(AccountSettingUiEvent.StartOAuthLogout) }
			.onFailure { startEvent(AccountSettingUiEvent.MakeToast(R.string.sign_out_fail)) }
	}

	private fun withdraw() = viewModelScope.launch {
		runCatching { withdrawUseCase() }
			.onSuccess { startEvent(AccountSettingUiEvent.StartOAuthWithdraw) }
			.onFailure { startEvent(AccountSettingUiEvent.MakeToast(R.string.withdraw_fail)) }
	}

	fun onSuccessOAuthLogout() {
		startEvent(AccountSettingUiEvent.MoveToLoginPage)
	}

	fun onSuccessOAuthWithdraw() {
		startEvent(AccountSettingUiEvent.MakeToast(R.string.withdraw_success))
		startEvent(AccountSettingUiEvent.MoveToLoginPage)
	}

	fun onClickLogoutBtn() {
		logout()
	}

	fun onClickWithdrawConfirm() {
		withdraw()
	}

	fun onClickWithdrawBtn() {
		startEvent(AccountSettingUiEvent.ShowWithdrawWarningDialog)
	}

	private fun startEvent(event: AccountSettingUiEvent) = viewModelScope.launch {
		_eventFlow.emit(event)
	}
}