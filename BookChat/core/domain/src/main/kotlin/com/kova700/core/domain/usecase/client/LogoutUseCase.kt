package com.kova700.core.domain.usecase.client

import com.kova700.bookchat.core.data.client.external.ClientRepository
import com.kova700.bookchat.core.data.fcm_token.external.FCMTokenRepository
import javax.inject.Inject

//TODO : [FixWaiting] DeviceID가져와서 서버에게 보내기( DeviceID가 같은 경우만 FCM 토큰 삭제되게 수정)
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