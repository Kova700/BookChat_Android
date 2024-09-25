package com.kova700.core.domain.usecase.client

import com.kova700.bookchat.core.data.client.external.ClientRepository
import com.kova700.bookchat.core.data.fcm_token.external.FCMTokenRepository
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