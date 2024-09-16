package com.kova700.bookchat.core.network.bookchat.search.model.book.both

import com.google.gson.annotations.SerializedName

enum class BookSearchSortOptionNetWork {
	@SerializedName("ACCURACY")
	ACCURACY,
	@SerializedName("LATEST")
	LATEST
}