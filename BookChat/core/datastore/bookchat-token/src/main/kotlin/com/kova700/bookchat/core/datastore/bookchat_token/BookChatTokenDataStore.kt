package com.kova700.bookchat.core.datastore.bookchat_token

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.kova700.bookchat.core.data.bookchat_token.external.model.BookChatToken
import com.kova700.bookchat.core.datastore.datastore.clearData
import com.kova700.bookchat.core.datastore.datastore.getDataFlow
import com.kova700.bookchat.core.datastore.datastore.setData
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

class BookChatTokenDataStore @Inject constructor(
	private val dataStore: DataStore<Preferences>,
) {
	private val bookChatTokenKey = stringPreferencesKey(BOOKCHAT_TOKEN_KEY)

	//TODO : 토큰 암호화 추가
	suspend fun getBookChatToken(): BookChatToken? {
		val tokenString = dataStore.getDataFlow(bookChatTokenKey).firstOrNull()
		if (tokenString.isNullOrBlank()) return null
		return Json.decodeFromString<BookChatToken>(tokenString)
	}

	suspend fun saveBookChatToken(token: BookChatToken) {
		val newToken = if (token.accessToken.startsWith(TOKEN_PREFIX).not())
			token.copy(accessToken = "$TOKEN_PREFIX ${token.accessToken}")
		else token
		dataStore.setData(bookChatTokenKey, Json.encodeToString(newToken))
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