package com.example.bookchat.domain.usecase

import com.example.bookchat.domain.model.BookChatToken
import com.example.bookchat.domain.repository.BookChatTokenRepository
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