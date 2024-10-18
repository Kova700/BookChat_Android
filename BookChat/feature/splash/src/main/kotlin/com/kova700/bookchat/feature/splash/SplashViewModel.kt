package com.kova700.bookchat.feature.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kova700.bookchat.core.data.bookchat_token.external.repository.BookChatTokenRepository
import com.kova700.bookchat.core.data.client.external.ClientRepository
import com.kova700.bookchat.core.network_manager.external.NetworkManager
import com.kova700.bookchat.core.network_manager.external.model.NetworkState
import com.kova700.bookchat.core.remoteconfig.RemoteConfigManager
import com.kova700.bookchat.feature.splash.SplashUiState.UiState
import com.kova700.core.domain.usecase.client.LogoutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

//TODO : [FixWaiting] Firebase- RemoteConfig 이용해서 (O)
//    서버 점검중 Dialog (ServerMaintenanceNoticeDialog)추가 (O)
//    (강조) Notification 누르고 ChannelActivity로 이동해도 서버 점검중 Dialog 띄울 수 있어야함 (X)

//TODO : [FixWaiting] 강제 업데이트 추가하기 (X)
@HiltViewModel
class SplashViewModel @Inject constructor(
	private val clientRepository: ClientRepository,
	private val bookChatTokenRepository: BookChatTokenRepository,
	private val logoutUseCase: LogoutUseCase,
	private val remoteConfigManager: RemoteConfigManager,
	private val networkManager: NetworkManager
) : ViewModel() {

	private val _eventFlow = MutableSharedFlow<SplashEvent>()
	val eventFlow = _eventFlow.asSharedFlow()

	private val _uiState = MutableStateFlow<SplashUiState>(SplashUiState.DEFAULT)
	val uiState = _uiState.asStateFlow()

	init {
		observeNetworkState()
	}

	private fun initUiState() = viewModelScope.launch {
		if (networkManager.isNetworkAvailable().not()) return@launch
		delay(SPLASH_DURATION.seconds)
		if (isBookChatAvailable()) startLogin()
	}

	private fun observeNetworkState() = viewModelScope.launch {
		networkManager.observeNetworkState().collect { state ->
			updateState { copy(isNetworkConnected = state == NetworkState.CONNECTED) }
			when (state) {
				NetworkState.CONNECTED -> initUiState()
				NetworkState.DISCONNECTED -> Unit
			}
		}
	}

	private fun startLogin() = viewModelScope.launch {
		if (uiState.value.isLoading) return@launch
		updateState { copy(uiState = UiState.LOADING) }
		if (isBookChatTokenExist()) getClientProfile()
		else startEvent(SplashEvent.MoveToLogin)
	}

	private suspend fun isBookChatAvailable(): Boolean {
		val remoteConfigValues = getRemoteConfig().await() ?: return true
		when {
			remoteConfigValues.isServerEnabled.not() -> {
				startEvent(
					SplashEvent.ShowServerDisabledDialog(
						message = remoteConfigValues.serverDownNoticeMessage
					)
				)
				return false
			}

			remoteConfigValues.isServerUnderMaintenance -> {
				startEvent(
					SplashEvent.ShowServerMaintenanceDialog(
						message = remoteConfigValues.serverUnderMaintenanceNoticeMessage
					)
				)
				return false
			}
		}
		return true
	}

	private fun getRemoteConfig() = viewModelScope.async {
		runCatching { remoteConfigManager.getRemoteConfig() }.getOrNull()
	}

	private suspend fun isBookChatTokenExist(): Boolean {
		return runCatching { bookChatTokenRepository.isBookChatTokenExist() }
			.getOrDefault(false)
	}

	private fun getClientProfile() = viewModelScope.launch {
		runCatching { clientRepository.getClientProfile() }
			.onSuccess { startEvent(SplashEvent.MoveToMain) }
			.onFailure {
				updateState { copy(uiState = UiState.ERROR) }
				clearAllData()
			}
	}

	//TODO : [FixWaiting] DeviceID가져와서 서버에게 보내기( DeviceID가 같은 경우만 FCM 토큰 삭제되게 수정)
	// 수정되면 needServer = true로 변경
	private fun clearAllData() = viewModelScope.launch {
		runCatching { logoutUseCase(needServer = false) }
			.also { startEvent(SplashEvent.MoveToLogin) }
	}

	private fun startEvent(event: SplashEvent) = viewModelScope.launch {
		_eventFlow.emit(event)
	}

	private inline fun updateState(block: SplashUiState.() -> SplashUiState) {
		_uiState.update { _uiState.value.block() }
	}

	companion object {
		private const val SPLASH_DURATION = 1.5
	}
}