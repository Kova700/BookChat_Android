package com.kova700.bookchat.core.network.bookchat.bookshelf.model.response

import com.google.gson.annotations.SerializedName

data class ResponseGetBookShelfBooks(
	@SerializedName("contents")
	val contents: List<BookShelfItemResponse>,
	@SerializedName("pageMeta")
	val pageMeta: BookShelfMeta,
)
