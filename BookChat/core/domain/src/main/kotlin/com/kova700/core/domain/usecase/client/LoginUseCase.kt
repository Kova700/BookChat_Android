package com.kova700.core.domain.usecase.client

import com.kova700.bookchat.core.data.bookchat_token.external.repository.BookChatTokenRepository
import com.kova700.bookchat.core.data.client.external.ClientRepository
import com.kova700.bookchat.core.data.deviceinfo.external.DeviceIDRepository
import com.kova700.bookchat.core.data.fcm_token.external.FCMTokenRepository
import com.kova700.bookchat.core.data.oauth.external.OAuthIdTokenRepository
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