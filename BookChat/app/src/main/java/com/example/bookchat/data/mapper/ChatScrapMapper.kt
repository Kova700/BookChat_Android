package com.example.bookchat.data.mapper

import com.example.bookchat.data.network.model.response.ChatScrapResponse
import com.example.bookchat.domain.model.ChatScrap

fun ChatScrapResponse.toDomain(): ChatScrap {
	return ChatScrap(
		scrapId = scrapId,
		scrapContent = scrapContent
	)
}