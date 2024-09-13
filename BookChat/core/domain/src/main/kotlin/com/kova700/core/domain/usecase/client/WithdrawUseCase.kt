package com.kova700.core.domain.usecase.client

import com.kova700.bookchat.core.data.client.external.ClientRepository
import com.kova700.bookchat.core.data.fcm_token.external.FCMTokenRepository
import javax.inject.Inject

class WithdrawUseCase @Inject constructor(
	private val clientRepository: ClientRepository,
	private val clearLocalDataUseCase: ClearLocalDataUseCase,
	private val fcmTokenRepository: FCMTokenRepository,
) {
	suspend operator fun invoke() {
		clientRepository.withdraw()
		clearLocalDataUseCase()
		runCatching { fcmTokenRepository.expireFCMToken() }
	}
}