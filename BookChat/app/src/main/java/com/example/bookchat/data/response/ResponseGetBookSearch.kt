package com.example.bookchat.data.response

import com.google.gson.annotations.SerializedName

data class ResponseGetBookSearch(
	@SerializedName("bookResponses")
    val bookResponses :List<BookResponse>,
	@SerializedName("meta")
    val searchingMeta : SearchingMeta
)