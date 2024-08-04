package com.example.bookchat.domain.usecase

import com.example.bookchat.domain.repository.ClientRepository
import com.example.bookchat.fcm.repository.external.FCMTokenRepository
import javax.inject.Inject

class LogoutUseCase @Inject constructor(
	private val clientRepository: ClientRepository,
	private val clearLocalDataUseCase: ClearLocalDataUseCase,
	private val fcmTokenRepository: FCMTokenRepository,
) {
	suspend operator fun invoke(needServer: Boolean = true) {
		if (needServer) clientRepository.logout()
		clearLocalDataUseCase()
		runCatching { fcmTokenRepository.expireFCMToken() }
	}
}