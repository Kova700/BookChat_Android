package com.example.bookchat.domain.usecase

import com.example.bookchat.data.mapper.toNetWork
import com.example.bookchat.data.mapper.toNetwork
import com.example.bookchat.data.network.BookChatApi
import com.example.bookchat.data.network.model.request.RequestUserSignUp
import com.example.bookchat.data.repository.ClientRepositoryImpl
import com.example.bookchat.domain.model.ReadingTaste
import com.example.bookchat.domain.repository.OAuthIdTokenRepository
import com.example.bookchat.utils.toMultiPartBody
import javax.inject.Inject

class SignUpUseCase @Inject constructor(
	private val bookChatApi: BookChatApi,
	private val oAuthIdTokenRepository: OAuthIdTokenRepository,
) {

	suspend operator fun invoke(
		nickname: String,
		readingTastes: List<ReadingTaste>,
		userProfile: ByteArray?,
	) {
		val idToken = oAuthIdTokenRepository.getIdToken()
		val requestUserSignUp = RequestUserSignUp(
			oauth2Provider = idToken.oAuth2Provider.toNetwork(),
			nickname = nickname,
			readingTastes = readingTastes.map { it.toNetWork() },
		)

		bookChatApi.signUp(
			idToken = idToken.token,
			userProfileImage = userProfile?.toMultiPartBody(
				contentType = ClientRepositoryImpl.CONTENT_TYPE_IMAGE_WEBP,
				multipartName = ClientRepositoryImpl.PROFILE_IMAGE_MULTIPART_NAME,
				fileName = ClientRepositoryImpl.PROFILE_IMAGE_FILE_NAME,
				fileExtension = ClientRepositoryImpl.PROFILE_IMAGE_FILE_EXTENSION
			),
			requestUserSignUp = requestUserSignUp
		)
	}
}