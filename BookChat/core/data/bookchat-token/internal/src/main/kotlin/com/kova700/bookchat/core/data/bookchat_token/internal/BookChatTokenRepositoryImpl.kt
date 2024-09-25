package com.kova700.bookchat.core.data.bookchat_token.internal

import com.kova700.bookchat.core.data.bookchat_token.external.model.BookChatToken
import com.kova700.bookchat.core.data.bookchat_token.external.repository.BookChatTokenRepository
import com.kova700.bookchat.core.datastore.bookchat_token.BookChatTokenDataStore
import javax.inject.Inject

class BookChatTokenRepositoryImpl @Inject constructor(
	private val dataStore: BookChatTokenDataStore,
) : BookChatTokenRepository {

	override suspend fun getBookChatToken(): BookChatToken? {
		return dataStore.getBookChatToken()
	}

	override suspend fun saveBookChatToken(token: BookChatToken) {
		dataStore.saveBookChatToken(token)
	}

	override suspend fun saveBookChatToken(accessToken: String, refreshToken: String) {
		dataStore.saveBookChatToken(accessToken, refreshToken)
	}

	override suspend fun isBookChatTokenExist(): Boolean {
		return dataStore.isBookChatTokenExist()
	}

	override suspend fun clear() {
		dataStore.clear()
	}
}