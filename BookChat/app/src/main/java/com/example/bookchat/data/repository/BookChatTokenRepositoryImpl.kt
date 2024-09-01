package com.example.bookchat.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.kova700.bookchat.core.datastore.datastore.clearData
import com.kova700.bookchat.core.datastore.datastore.getDataFlow
import com.kova700.bookchat.core.datastore.datastore.setData
import com.example.bookchat.domain.model.BookChatToken
import com.example.bookchat.domain.repository.BookChatTokenRepository
import com.example.bookchat.domain.repository.BookChatTokenRepository.Companion.TOKEN_PREFIX
import com.google.gson.Gson
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class BookChatTokenRepositoryImpl @Inject constructor(
	private val dataStore: DataStore<Preferences>,
	private val gson: Gson,
) : BookChatTokenRepository {
	private val bookChatTokenKey = stringPreferencesKey(BOOKCHAT_TOKEN_KEY)

	//TODO : 토큰 암호화 추가
	override suspend fun getBookChatToken(): BookChatToken? {
		val tokenString = dataStore.getDataFlow(bookChatTokenKey).firstOrNull()
		if (tokenString.isNullOrBlank()) return null
		return gson.fromJson(tokenString, BookChatToken::class.java)
	}

	override suspend fun saveBookChatToken(token: BookChatToken) {
		val newToken = if (token.accessToken.startsWith(TOKEN_PREFIX).not())
			token.copy(accessToken = "$TOKEN_PREFIX ${token.accessToken}")
		else token
		dataStore.setData(bookChatTokenKey, gson.toJson(newToken))
	}

	override suspend fun isBookChatTokenExist(): Boolean {
		return getBookChatToken() != null
	}

	override suspend fun clear() {
		dataStore.clearData(bookChatTokenKey)
	}

	companion object {
		private const val BOOKCHAT_TOKEN_KEY = "BOOKCHAT_TOKEN_KEY"
	}
}