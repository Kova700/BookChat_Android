package com.kova700.bookchat.core.data.bookchat_token.external.repository

import com.kova700.bookchat.core.data.bookchat_token.external.model.BookChatToken

interface BookChatTokenRepository {
	suspend fun getBookChatToken(): BookChatToken?
	suspend fun saveBookChatToken(token: BookChatToken)
	suspend fun isBookChatTokenExist(): Boolean
	suspend fun clear()

	companion object {
		const val TOKEN_PREFIX = "Bearer"
	}
}