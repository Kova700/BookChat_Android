package com.kova700.core.domain.usecase.client

import com.kova700.bookchat.core.data.client.external.ClientRepository
import com.kova700.bookchat.core.data.client.external.model.ReadingTaste
import com.kova700.bookchat.core.data.oauth.external.OAuthIdTokenRepository
import javax.inject.Inject

class SignUpUseCase @Inject constructor(
	private val clientRepository: ClientRepository,
	private val oAuthIdTokenRepository: OAuthIdTokenRepository,
) {
	suspend operator fun invoke(
		nickname: String,
		readingTastes: List<ReadingTaste>,
		userProfile: ByteArray?,
	) {
		clientRepository.signUp(
			idToken = oAuthIdTokenRepository.getIdToken(),
			nickname = nickname,
			readingTastes = readingTastes,
			userProfile = userProfile,
		)
	}
}