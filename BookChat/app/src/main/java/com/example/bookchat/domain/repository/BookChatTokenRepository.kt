package com.example.bookchat.domain.repository

import com.example.bookchat.domain.model.BookChatToken

interface BookChatTokenRepository {
	suspend fun getBookChatToken(): BookChatToken?
	suspend fun saveBookChatToken(token: BookChatToken)
	suspend fun isBookChatTokenExist(): Boolean
	suspend fun clear()

	companion object {
		const val TOKEN_PREFIX = "Bearer"
	}
}