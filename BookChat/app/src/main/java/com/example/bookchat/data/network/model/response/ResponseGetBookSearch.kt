package com.example.bookchat.data.network.model.response

import com.google.gson.annotations.SerializedName

data class ResponseGetBookSearch(
	@SerializedName("bookResponses")
    val bookSearchResponse :List<com.example.bookchat.data.network.model.response.BookSearchResponse>,
	@SerializedName("meta")
    val searchingMeta : SearchingMeta
)