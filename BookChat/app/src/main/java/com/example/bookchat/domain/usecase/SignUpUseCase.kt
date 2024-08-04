package com.example.bookchat.domain.usecase

import com.example.bookchat.domain.model.ReadingTaste
import com.example.bookchat.domain.repository.ClientRepository
import com.example.bookchat.oauth.repository.external.OAuthIdTokenRepository
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