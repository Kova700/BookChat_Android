package com.example.bookchat.domain.usecase

import com.kova700.bookchat.core.data.bookchat_token.external.model.BookChatToken
import com.kova700.bookchat.core.data.bookchat_token.external.repository.BookChatTokenRepository
import com.example.bookchat.domain.repository.ClientRepository
import java.io.IOException
import javax.inject.Inject

class RenewBookChatTokenUseCase @Inject constructor(
	private val clientRepository: ClientRepository,
	private val bookChatTokenRepository: BookChatTokenRepository,
) {
	suspend operator fun invoke(): BookChatToken {
		val currentToken = bookChatTokenRepository.getBookChatToken()
			?: throw IOException("BookChatToken does not exist.")
		val newToken = clientRepository.renewBookChatToken(currentToken)
		bookChatTokenRepository.saveBookChatToken(newToken)
		return newToken
	}
}