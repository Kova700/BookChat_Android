package com.example.bookchat.data.network.model.response

import com.google.gson.annotations.SerializedName

data class ResponseGetBookShelfBooks(
	@SerializedName("contents")
    val contents :List<com.example.bookchat.data.network.model.response.BookShelfItemResponse>,
	@SerializedName("pageMeta")
    val pageMeta : com.example.bookchat.data.network.model.response.BookShelfMeta,
)
