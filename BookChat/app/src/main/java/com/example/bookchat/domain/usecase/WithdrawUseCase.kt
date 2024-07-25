package com.example.bookchat.domain.usecase

import com.example.bookchat.domain.repository.ClientRepository
import com.example.bookchat.domain.repository.FCMTokenRepository
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