package com.kova700.bookchat.core.network.bookchat.search.model.book.both

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class BookSearchSortOptionNetWork {
	@SerialName("ACCURACY")
	ACCURACY,

	@SerialName("LATEST")
	LATEST
}