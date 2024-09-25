package com.kova700.bookchat.core.datastore.bookchat_token.mapper

import com.kova700.bookchat.core.data.bookchat_token.external.model.BookChatToken
import com.kova700.bookchat.core.datastore.bookchat_token.model.BookChatTokenEntity

fun BookChatToken.toEntity(): BookChatTokenEntity {
	return BookChatTokenEntity(
		accessToken = accessToken,
		refreshToken = refreshToken,
	)
}

fun BookChatTokenEntity.toDomain(): BookChatToken {
	return BookChatToken(
		accessToken = accessToken,
		refreshToken = refreshToken,
	)
}