package com.example.bookchat.data.network.model.response

import com.example.bookchat.domain.model.StarRating
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class BookShelfItemResponse(
	@SerializedName("bookShelfId")
	val bookShelfId: Long,
	@SerializedName("title")
	val title: String,
	@SerializedName("isbn")
	val isbn: String,
	@SerializedName("bookCoverImageUrl")
	val bookCoverImageUrl: String,
	@SerializedName("authors")
	val authors: List<String>,
	@SerializedName("publisher")
	val publisher: String,
	@SerializedName("publishAt")
	val publishAt: String,
	@SerializedName("pages")
	var pages: Int,
	@SerializedName("star")
	var star: StarRating?,
) : Serializable