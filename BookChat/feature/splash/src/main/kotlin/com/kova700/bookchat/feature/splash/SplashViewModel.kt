package com.kova700.bookchat.feature.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kova700.bookchat.core.data.bookchat_token.external.repository.BookChatTokenRepository
import com.kova700.bookchat.core.data.client.external.ClientRepository
import com.kova700.bookchat.core.data.fcm_token.external.FCMTokenRepository
import com.kova700.core.domain.usecase.client.ClearLocalDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

//TODO : Firebase- RemoteConfig 이용해서
//    서버 점검중 Dialog (ServerMaintenanceNoticeDialog)추가
//    + Flag추가 해서 Notification 막기
//    + 기존에 있던 notificaiton 눌러도 지워지지 않고, 서버 점검중 Dialog 띄우기
@HiltViewModel
class SplashViewModel @Inject constructor(
	private val clientRepository: ClientRepository,
	private val bookChatTokenRepository: BookChatTokenRepository,
	private val fcmTokenRepository: FCMTokenRepository,
	private val clearLocalDataUseCase: ClearLocalDataUseCase,
) : ViewModel() {

	private val _eventFlow = MutableSharedFlow<SplashEvent>()
	val eventFlow = _eventFlow.asSharedFlow()

	init {
		viewModelScope.launch {
			delay(SPLASH_DURATION.seconds)
			if (isBookChatTokenExist()) requestUserInfo()
			else startEvent(SplashEvent.MoveToLogin)
		}
	}

	private suspend fun isBookChatTokenExist(): Boolean {
		return runCatching { bookChatTokenRepository.isBookChatTokenExist() }
			.getOrDefault(false)
	}

	private fun requestUserInfo() = viewModelScope.launch {
		runCatching { clientRepository.getClientProfile() }
			.onSuccess { startEvent(SplashEvent.MoveToMain) }
			.onFailure { clearAllData() }
	}

	private fun clearAllData() = viewModelScope.launch {
		runCatching {
			clearLocalDataUseCase()
			fcmTokenRepository.expireFCMToken()
		}.also { startEvent(SplashEvent.MoveToLogin) }
	}

	private fun startEvent(event: SplashEvent) = viewModelScope.launch {
		_eventFlow.emit(event)
	}

	companion object {
		const val SPLASH_DURATION = 1.5
	}
}