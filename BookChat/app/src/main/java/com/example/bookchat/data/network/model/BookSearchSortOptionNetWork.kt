package com.example.bookchat.data.network.model

import com.google.gson.annotations.SerializedName

enum class BookSearchSortOptionNetWork {
	@SerializedName("ACCURACY")
	ACCURACY,
	@SerializedName("LATEST")
	LATEST
}