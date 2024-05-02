package com.example.bookchat.data.model

import com.google.gson.annotations.SerializedName

enum class BookSearchSortOptionNetWork {
	@SerializedName("ACCURACY")
	ACCURACY,
	@SerializedName("LATEST")
	LATEST
}