package com.example.bookchat.data.mapper

import com.example.bookchat.data.response.BookChatTokenResponse
import com.example.bookchat.domain.model.BookChatToken

fun BookChatTokenResponse.toBookChatToken(): BookChatToken {
	return BookChatToken(accessToken, refreshToken)
}