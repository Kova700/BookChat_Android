package com.example.bookchat.data.network.model.response

import com.google.gson.annotations.SerializedName
import java.io.Serializable

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
	val bookCoverImageUrl: String
) : Serializable