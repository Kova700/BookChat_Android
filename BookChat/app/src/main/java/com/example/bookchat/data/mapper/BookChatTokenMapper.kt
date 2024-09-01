package com.example.bookchat.data.mapper

import com.example.bookchat.data.network.model.response.BookChatTokenResponse
import com.kova700.bookchat.core.data.bookchat_token.external.model.BookChatToken

fun BookChatTokenResponse.toBookChatToken(): BookChatToken {
	return BookChatToken(accessToken, refreshToken)
}