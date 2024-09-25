package com.kova700.bookchat.core.network.bookchat.search.model.book.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ResponseGetBookSearch(
	@SerialName("bookResponses")
	val bookSearchResponse: List<BookSearchResponse>,
	@SerialName("meta")
	val searchingMeta: SearchingMeta,
)