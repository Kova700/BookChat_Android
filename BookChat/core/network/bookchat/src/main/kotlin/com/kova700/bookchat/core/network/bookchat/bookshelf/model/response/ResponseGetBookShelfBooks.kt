package com.kova700.bookchat.core.network.bookchat.bookshelf.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ResponseGetBookShelfBooks(
	@SerialName("contents")
	val contents: List<BookShelfItemResponse>,
	@SerialName("pageMeta")
	val pageMeta: BookShelfMeta,
)
