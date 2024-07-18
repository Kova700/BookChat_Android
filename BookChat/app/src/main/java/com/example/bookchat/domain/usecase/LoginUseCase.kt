package com.example.bookchat.domain.usecase

import com.example.bookchat.data.mapper.toBookChatToken
import com.example.bookchat.data.mapper.toNetwork
import com.example.bookchat.data.network.BookChatApi
import com.example.bookchat.data.network.model.request.RequestUserLogin
import com.example.bookchat.data.network.model.response.NeedToDeviceWarningException
import com.example.bookchat.data.network.model.response.NeedToSignUpException
import com.example.bookchat.domain.repository.BookChatTokenRepository
import com.example.bookchat.domain.repository.DeviceIDRepository
import com.example.bookchat.domain.repository.FCMTokenRepository
import com.example.bookchat.domain.repository.OAuthIdTokenRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
	private val bookChatApi: BookChatApi,
	private val bookChatTokenRepository: BookChatTokenRepository,
	private val fcmTokenRepository: FCMTokenRepository,
	private val deviceIdRepository: DeviceIDRepository,
	private val oAuthIdTokenRepository: OAuthIdTokenRepository,
) {
	suspend operator fun invoke(isDeviceChangeApproved: Boolean = false) {
		val idToken = oAuthIdTokenRepository.getIdToken()
		val requestUserLogin = RequestUserLogin(
			fcmToken = fcmTokenRepository.getFCMToken().text,
			deviceToken = deviceIdRepository.getDeviceID(),
			isDeviceChangeApproved = isDeviceChangeApproved,
			oauth2Provider = idToken.oAuth2Provider.toNetwork()
		)

		val response = bookChatApi.login(idToken.token, requestUserLogin)
		if (response.isSuccessful) {
			response.body()?.toBookChatToken()?.let {
				bookChatTokenRepository.saveBookChatToken(it)
				return
			}
		}
		when (response.code()) {
			404 -> throw NeedToSignUpException(response.errorBody()?.string())
			409 -> throw NeedToDeviceWarningException(response.errorBody()?.string())
		}
	}
}