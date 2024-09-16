package com.kova700.bookchat.core.network.bookchat.search.model.book.response

import com.google.gson.annotations.SerializedName

data class BookSearchResponse(
	@SerializedName("isbn")
	val isbn: String,
	@SerializedName("title")
	var title: String,
	@SerializedName("authors")
	val authors: List<String>,
	@SerializedName("publisher")
	val publisher: String,
	@SerializedName("publishAt")
	var publishAt: String,
	@SerializedName("bookCoverImageUrl")
	val bookCoverImageUrl: String,
)