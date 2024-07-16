package com.example.bookchat.domain.usecase

import com.example.bookchat.data.mapper.toBookChatToken
import com.example.bookchat.data.network.BookChatApi
import com.example.bookchat.domain.model.BookChatToken
import com.example.bookchat.domain.repository.BookChatTokenRepository
import java.io.IOException
import javax.inject.Inject

class RenewBookChatTokenUseCase @Inject constructor(
	private val bookChatApi: BookChatApi,
	private val bookChatTokenRepository: BookChatTokenRepository,
) {
	suspend operator fun invoke(): BookChatToken {
		val refreshToken = bookChatTokenRepository.getBookChatToken()?.refreshToken
			?: throw IOException("Refresh Token does not exist.")
		val newToken = bookChatApi.renewBookChatToken(refreshToken).toBookChatToken()
		bookChatTokenRepository.saveBookChatToken(newToken)
		return newToken
	}
}