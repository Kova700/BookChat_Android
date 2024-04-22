package com.example.bookchat.data.network

import com.example.bookchat.BuildConfig.TOKEN_RENEWAL_URL
import com.example.bookchat.data.response.BadRequestException
import com.example.bookchat.data.response.ForbiddenException
import com.example.bookchat.data.response.TokenRenewalFailException
import com.example.bookchat.domain.model.BookChatToken
import com.example.bookchat.domain.repository.BookChatTokenRepository
import com.google.gson.Gson
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import javax.inject.Inject

class AppInterceptor @Inject constructor(
	private val bookChatTokenRepository: BookChatTokenRepository,
	private val gson: Gson
) : Interceptor {

	override fun intercept(
		chain: Interceptor.Chain
	): Response {
		val bookchatToken = runBlocking { bookChatTokenRepository.getBookChatToken() }
		var response = chain.requestWithAccessToken(bookchatToken)
		if (response.isSuccessful) return response

		if (response.isTokenExpired()) {
			val newToken = chain.renewToken(bookchatToken)
			response = chain.requestWithAccessToken(newToken)
			if (response.isSuccessful) return response
		}

		response.getException()?.let { throw it }
		return response
	}

	private fun Interceptor.Chain.renewToken(bookchatToken: BookChatToken?): BookChatToken {
		val refreshToken = bookchatToken?.refreshToken
		val requestWithRefreshToken =
			getNewRequest(
				requestBody = getJsonRequestBody(
					content = refreshToken,
					contentType = CONTENT_TYPE_JSON
				),
				requestUrl = TOKEN_RENEWAL_URL
			)

		val response = proceed(requestWithRefreshToken)

		// TODO : 리프레시 토큰마저 만료되었음으로 새로 로그인 해야함
		//  모든 작업 종료하고 로그인 페이지로 이동하게 수정
		if (response.isSuccessful.not()) throw TokenRenewalFailException()

		val newToken = response.parseToToken()
		runBlocking { bookChatTokenRepository.saveBookChatToken(newToken) }
		return newToken
	}

	private fun Interceptor.Chain.requestWithAccessToken(bookChatToken: BookChatToken?): Response {
		val accessToken = bookChatToken?.accessToken
		val requestWithToken = request().addHeader(AUTHORIZATION, accessToken ?: "")
		return proceed(requestWithToken)
	}

	//TODO :요청 하는 곳 마다 전부 예외처리 되어있는지 확인해보자.
	// 채팅 connect요청도 이거임(예외처리해야함)
	private fun Response.getException(): Exception? {
		return when (code) {
			400 -> BadRequestException(message)
			403 -> ForbiddenException(message)
			else -> null
		}
	}

	private fun Request.addHeader(headerName: String, headerContent: String): Request {
		return newBuilder()
			.addHeader(headerName, headerContent)
			.build()
	}

	private fun getNewRequest(
		requestBody: RequestBody,
		requestUrl: String
	): Request {
		return Request.Builder()
			.url(requestUrl)
			.post(requestBody)
			.build()
	}

	private fun Response.isTokenExpired(): Boolean {
		return this.code == 401
	}

	private fun <T> getJsonRequestBody(
		content: T,
		contentType: String
	): RequestBody {
		val jsonString = gson.toJson(content)
		return jsonString.toRequestBody(contentType.toMediaTypeOrNull())
	}

	private fun Response.parseToToken(): BookChatToken {
		return gson.fromJson(body?.string(), BookChatToken::class.java)
	}

	companion object {
		private const val AUTHORIZATION = "Authorization"
		private const val CONTENT_TYPE_JSON = "application/json; charset=utf-8"
	}
}