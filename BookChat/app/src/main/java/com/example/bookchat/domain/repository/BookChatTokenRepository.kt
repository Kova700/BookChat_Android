package com.example.bookchat.domain.repository

import com.example.bookchat.domain.model.BookChatToken

interface BookChatTokenRepository {
	suspend fun getBookChatToken(): BookChatToken?
	suspend fun renewBookChatToken(): BookChatToken?
	suspend fun saveBookChatToken(token: BookChatToken)
	suspend fun isBookChatTokenExist(): Boolean
	suspend fun clearBookChatToken()
}