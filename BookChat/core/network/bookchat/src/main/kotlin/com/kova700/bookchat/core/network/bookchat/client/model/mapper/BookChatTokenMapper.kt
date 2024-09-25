package com.kova700.bookchat.core.network.bookchat.client.model.mapper

import com.kova700.bookchat.core.data.bookchat_token.external.model.BookChatToken
import com.kova700.bookchat.core.network.bookchat.client.model.response.BookChatTokenResponse

fun BookChatTokenResponse.toBookChatToken(): BookChatToken {
	return BookChatToken(
		accessToken = accessToken,
		refreshToken = refreshToken
	)
}