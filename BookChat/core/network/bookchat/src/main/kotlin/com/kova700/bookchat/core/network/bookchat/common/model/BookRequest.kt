package com.kova700.bookchat.core.network.bookchat.common.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BookRequest(
	@SerialName("isbn")
	val isbn: String,
	@SerialName("title")
	var title: String,
	@SerialName("authors")
	val authors: List<String>,
	@SerialName("publisher")
	val publisher: String,
	@SerialName("publishAt")
	var publishAt: String,
	@SerialName("bookCoverImageUrl")
	val bookCoverImageUrl: String,
)