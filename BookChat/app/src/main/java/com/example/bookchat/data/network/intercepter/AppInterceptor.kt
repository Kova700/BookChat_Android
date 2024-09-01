package com.example.bookchat.data.network.intercepter

import com.kova700.bookchat.core.data.bookchat_token.external.repository.BookChatTokenRepository
import com.google.gson.Gson
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AppInterceptor @Inject constructor(
	private val bookChatTokenRepository: BookChatTokenRepository,
	private val gson: Gson,
) : Interceptor {

	override fun intercept(
		chain: Interceptor.Chain,
	): Response {
		val bookchatToken = runBlocking { bookChatTokenRepository.getBookChatToken() }
		var response = chain.requestWithAccessToken(bookchatToken)
		if (response.isSuccessful) return response

		if (response.isTokenExpired()) {
			val newToken = chain.renewToken(
				bookchatToken = bookchatToken,
				parser = gson
			)
			runBlocking { bookChatTokenRepository.saveBookChatToken(newToken) }
			response = chain.requestWithAccessToken(newToken)
			if (response.isSuccessful) return response
		}

		response.getException()?.let { throw it }
		return response
	}
}