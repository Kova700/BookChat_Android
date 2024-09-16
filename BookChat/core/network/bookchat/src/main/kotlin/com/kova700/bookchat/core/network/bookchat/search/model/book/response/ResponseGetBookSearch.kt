package com.kova700.bookchat.core.network.bookchat.search.model.book.response

import com.google.gson.annotations.SerializedName

data class ResponseGetBookSearch(
	@SerializedName("bookResponses")
	val bookSearchResponse: List<BookSearchResponse>,
	@SerializedName("meta")
	val searchingMeta: SearchingMeta,
)