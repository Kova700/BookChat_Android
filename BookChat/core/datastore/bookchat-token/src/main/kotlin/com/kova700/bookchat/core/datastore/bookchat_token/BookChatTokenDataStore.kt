package com.kova700.bookchat.core.datastore.bookchat_token

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.kova700.bookchat.core.data.bookchat_token.external.model.BookChatToken
import com.kova700.bookchat.core.datastore.bookchat_token.mapper.toDomain
import com.kova700.bookchat.core.datastore.bookchat_token.mapper.toEntity
import com.kova700.bookchat.core.datastore.bookchat_token.model.BookChatTokenEntity
import com.kova700.bookchat.core.datastore.datastore.CryptographyManager
import com.kova700.bookchat.core.datastore.datastore.clearData
import com.kova700.bookchat.core.datastore.datastore.getDataFlow
import com.kova700.bookchat.core.datastore.datastore.setData
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

class BookChatTokenDataStore @Inject constructor(
	private val dataStore: DataStore<Preferences>,
	private val jsonSerializer: Json,
	private val cryptographyManager: CryptographyManager
) {
	private val bookChatTokenKey = stringPreferencesKey(BOOKCHAT_TOKEN_KEY)

	suspend fun getBookChatToken(): BookChatToken? {
		val encryptedTokenString = dataStore.getDataFlow(bookChatTokenKey).firstOrNull()
			.takeUnless { it.isNullOrBlank() } ?: return null
		val decryptedTokenString = cryptographyManager.decrypt(encryptedTokenString)
		return jsonSerializer.decodeFromString<BookChatTokenEntity>(decryptedTokenString).toDomain()
	}

	suspend fun saveBookChatToken(token: BookChatToken) {
		val isTokenPrefixExist = token.accessToken.startsWith(TOKEN_PREFIX)
		val newToken = when {
			isTokenPrefixExist -> token
			else -> token.copy(accessToken = "$TOKEN_PREFIX ${token.accessToken}")
		}
		val tokenString = jsonSerializer.encodeToString(newToken.toEntity())
		val encryptedTokenString = cryptographyManager.encrypt(tokenString)
		dataStore.setData(bookChatTokenKey, encryptedTokenString)
	}

	suspend fun saveBookChatToken(accessToken: String, refreshToken: String) {
		saveBookChatToken(BookChatToken(accessToken, refreshToken))
	}

	suspend fun isBookChatTokenExist(): Boolean {
		return getBookChatToken() != null
	}

	suspend fun clear() {
		dataStore.clearData(bookChatTokenKey)
	}

	companion object {
		private const val BOOKCHAT_TOKEN_KEY = "BOOKCHAT_TOKEN_KEY"
		private const val TOKEN_PREFIX = "Bearer"
	}
}