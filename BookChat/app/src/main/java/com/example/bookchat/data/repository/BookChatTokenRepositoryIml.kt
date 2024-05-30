package com.example.bookchat.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.bookchat.data.datastore.clearData
import com.example.bookchat.data.datastore.getDataFlow
import com.example.bookchat.data.datastore.setData
import com.example.bookchat.domain.model.BookChatToken
import com.example.bookchat.domain.repository.BookChatTokenRepository
import com.google.gson.Gson
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class BookChatTokenRepositoryIml @Inject constructor(
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
		val tokenWithPrefix = token.copy(accessToken = "$TOKEN_PREFIX ${token.accessToken}")
		dataStore.setData(bookChatTokenKey, gson.toJson(tokenWithPrefix))
	}

	override suspend fun isBookChatTokenExist(): Boolean {
		return getBookChatToken() != null
	}

	override suspend fun clearBookChatToken() {
		dataStore.clearData(bookChatTokenKey)
	}

	companion object {
		private const val TOKEN_PREFIX = "Bearer"
		private const val BOOKCHAT_TOKEN_KEY = "BOOKCHAT_TOKEN_KEY"
	}
}