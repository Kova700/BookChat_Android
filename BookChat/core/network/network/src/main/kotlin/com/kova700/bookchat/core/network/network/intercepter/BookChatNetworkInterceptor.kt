package com.kova700.bookchat.core.network.network.intercepter

import com.kova700.bookchat.core.data.bookchat_token.external.repository.BookChatTokenRepository
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class BookChatNetworkInterceptor @Inject constructor(
	private val bookChatTokenRepository: BookChatTokenRepository,
) : Interceptor {

	override fun intercept(
		chain: Interceptor.Chain,
	): Response {
		val bookchatToken = runBlocking { bookChatTokenRepository.getBookChatToken() }
		var response = chain.requestWithAccessToken(bookchatToken)
		if (response.isSuccessful) return response

		if (response.isTokenExpired()) {
			val newToken = chain.renewToken(bookchatToken)
			runBlocking { bookChatTokenRepository.saveBookChatToken(newToken) }
			response = chain.requestWithAccessToken(newToken)
			if (response.isSuccessful) return response
		}

		response.getException()?.let { throw it }
		return response
	}
}