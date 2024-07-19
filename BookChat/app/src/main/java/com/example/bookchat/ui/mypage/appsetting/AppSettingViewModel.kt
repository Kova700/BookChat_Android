package com.example.bookchat.ui.mypage.appsetting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookchat.domain.repository.AppSettingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppSettingViewModel @Inject constructor(
	private val appSettingRepository: AppSettingRepository,
) : ViewModel() {

	private val _uiState = MutableStateFlow<AppSettingUiState>(AppSettingUiState.DEFAULT)
	val uiState get() = _uiState.asStateFlow()

	init {
		observeAppSetting()
	}

	private fun observeAppSetting() = viewModelScope.launch {
		appSettingRepository.observeAppSetting().collect { appSetting ->
			updateState { copy(isPushNotificationEnabled = appSetting.isPushNotificationEnabled) }
		}
	}

	private fun setNotificationSwitchState(isChecked: Boolean) = viewModelScope.launch {
		appSettingRepository.setPushNotificationMuteState(isChecked)
	}

	fun onChangeNotificationSwitchState(isChecked: Boolean) {
		if (uiState.value.isPushNotificationEnabled == isChecked) return
		setNotificationSwitchState(isChecked)
	}

	private inline fun updateState(block: AppSettingUiState.() -> AppSettingUiState) {
		_uiState.update { _uiState.value.block() }
	}
}