package com.example.bookchat.data.network.model.request

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class BookRequest(
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