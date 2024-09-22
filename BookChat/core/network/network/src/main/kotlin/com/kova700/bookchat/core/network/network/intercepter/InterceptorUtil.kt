package com.kova700.bookchat.core.network.network.intercepter

import com.kova700.bookchat.core.data.bookchat_token.external.model.BookChatToken
import com.kova700.bookchat.core.data.common.model.network.ForbiddenException
import com.kova700.bookchat.core.data.common.model.network.TokenRenewalFailException
import com.kova700.bookchat.core.datastore.bookchat_token.mapper.toDomain
import com.kova700.bookchat.core.datastore.bookchat_token.model.BookChatTokenEntity
import com.kova700.bookchat.core.network.network.BuildConfig.TOKEN_RENEWAL_URL
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response

private const val AUTHORIZATION = "Authorization"
private const val TOKEN_PREFIX = "Bearer"
private const val CONTENT_TYPE_JSON = "application/json; charset=utf-8"

fun Interceptor.Chain.renewToken(
	bookchatToken: BookChatToken?,
): BookChatToken {
	val refreshToken = bookchatToken?.refreshToken
	val requestWithRefreshToken = getNewRequest(
		requestBody = Json.getJsonRequestBody(
			content = refreshToken,
			contentType = CONTENT_TYPE_JSON
		),
		requestUrl = TOKEN_RENEWAL_URL
	)

	val response = proceed(requestWithRefreshToken)

	// TODO : 리프레시 토큰마저 만료되었음으로 새로 로그인 해야함
	//  모든 작업 종료하고 로그인 페이지로 이동하게 수정
	if (response.isSuccessful.not()) throw TokenRenewalFailException()
	return response.parseToBookChatToken()
}

private inline fun <reified T> Json.getJsonRequestBody(
	content: T,
	contentType: String,
): RequestBody {
	return encodeToString(content).toRequestBody(contentType.toMediaTypeOrNull())
}

private fun Request.addHeader(headerName: String, headerContent: String): Request {
	return newBuilder()
		.addHeader(headerName, headerContent)
		.build()
}

fun Interceptor.Chain.requestWithAccessToken(bookChatToken: BookChatToken?): Response {
	val accessToken = bookChatToken?.accessToken
	val requestWithToken = request().addHeader(AUTHORIZATION, accessToken ?: "")
	return proceed(requestWithToken)
}

//TODO :요청 하는 곳 마다 전부 예외처리 되어있는지 확인해보자.
// 채팅 connect요청도 이거임(예외처리해야함)
fun Response.getException(): Exception? {
	return when (code) {
		403 -> ForbiddenException(message)
		else -> null
	}
}

private fun getNewRequest(
	requestBody: RequestBody,
	requestUrl: String,
): Request {
	return Request.Builder()
		.url(requestUrl)
		.post(requestBody)
		.build()
}

fun Response.isTokenExpired(): Boolean {
	return this.code == 401
}

private fun Response.parseToBookChatToken(): BookChatToken {
	val token = Json.decodeFromString<BookChatTokenEntity>(
		body?.string() ?: throw TokenRenewalFailException()
	)
	return token.copy(accessToken = "$TOKEN_PREFIX ${token.accessToken}").toDomain()
}