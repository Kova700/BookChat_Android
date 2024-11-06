package com.kova700.bookchat.core.network.network.intercepter

import android.content.Context
import com.kova700.bookchat.core.data.bookchat_token.external.model.BookChatToken
import com.kova700.bookchat.core.data.bookchat_token.external.repository.BookChatTokenRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class BookChatNetworkInterceptor @Inject constructor(
	private val bookChatTokenRepository: BookChatTokenRepository,
	private val jsonSerializer: Json,
	@ApplicationContext private val context: Context,
) : Interceptor {

	override fun intercept(
		chain: Interceptor.Chain,
	): Response {
		val bookchatToken = getBookChatToken()
		var response = chain.requestWithAccessToken(bookchatToken)
		if (response.isSuccessful) return response

		if (response.isTokenExpired() && bookchatToken != null) {
			val newToken = chain.renewToken(
				currentBookchatToken = bookchatToken,
				jsonSerializer = jsonSerializer,
				context = context
			) ?: return response

			saveBookChatToken(newToken)
			response = chain.requestWithAccessToken(newToken)
			if (response.isSuccessful) return response
		}

		return response
	}

	private fun getBookChatToken(): BookChatToken? {
		return runBlocking { bookChatTokenRepository.getBookChatToken() }
	}

	private fun saveBookChatToken(bookChatToken: BookChatToken) {
		runBlocking { bookChatTokenRepository.saveBookChatToken(bookChatToken) }
	}
}