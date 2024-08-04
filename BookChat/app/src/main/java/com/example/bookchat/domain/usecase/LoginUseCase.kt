package com.example.bookchat.domain.usecase

import com.example.bookchat.domain.repository.BookChatTokenRepository
import com.example.bookchat.domain.repository.ClientRepository
import com.example.bookchat.domain.repository.DeviceIDRepository
import com.example.bookchat.fcm.repository.external.FCMTokenRepository
import com.example.bookchat.oauth.repository.external.OAuthIdTokenRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
	private val clientRepository: ClientRepository,
	private val bookChatTokenRepository: BookChatTokenRepository,
	private val fcmTokenRepository: FCMTokenRepository,
	private val deviceIdRepository: DeviceIDRepository,
	private val oAuthIdTokenRepository: OAuthIdTokenRepository,
) {
	suspend operator fun invoke(isDeviceChangeApproved: Boolean = false) {
		val bookchatToken = clientRepository.login(
			idToken = oAuthIdTokenRepository.getIdToken(),
			fcmToken = if (isDeviceChangeApproved) fcmTokenRepository.getNewFCMToken()
			else fcmTokenRepository.getFCMToken(),
			deviceUUID = deviceIdRepository.getDeviceID(),
			isDeviceChangeApproved = isDeviceChangeApproved
		)
		bookChatTokenRepository.saveBookChatToken(bookchatToken)
	}
}