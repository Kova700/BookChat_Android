package com.kova700.bookchat.core.network.network.intercepter

import android.content.Context
import com.kova700.bookchat.core.data.bookchat_token.external.model.BookChatToken
import com.kova700.bookchat.core.datastore.bookchat_token.mapper.toDomain
import com.kova700.bookchat.core.datastore.bookchat_token.model.BookChatTokenEntity
import com.kova700.bookchat.core.fcm.forced_logout.ForcedLogoutWorker
import com.kova700.bookchat.core.fcm.forced_logout.model.ForcedLogoutReason
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
	currentBookchatToken: BookChatToken,
	jsonSerializer: Json,
	context: Context,
): BookChatToken? {
	val refreshToken = currentBookchatToken.refreshToken
	val requestWithRefreshToken = getNewRequest(
		requestBody = jsonSerializer.getJsonRequestBody(
			content = refreshToken,
			contentType = CONTENT_TYPE_JSON
		),
		requestUrl = TOKEN_RENEWAL_URL
	)

	val response = proceed(requestWithRefreshToken)

	return when {
		response.isSuccessful -> response.parseToBookChatToken(jsonSerializer = jsonSerializer)
		response.isTokenExpired() -> {
			ForcedLogoutWorker.start(
				context = context,
				reason = ForcedLogoutReason.TOKEN_EXPIRED
			)
			null
		}

		else -> null
	}

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

private fun Response.parseToBookChatToken(jsonSerializer: Json): BookChatToken? {
	val token = jsonSerializer.decodeFromString<BookChatTokenEntity>(
		body?.string() ?: return null
	)
	return token.copy(accessToken = "$TOKEN_PREFIX ${token.accessToken}").toDomain()
}