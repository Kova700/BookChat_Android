package com.example.bookchat.data.response

import com.example.bookchat.data.BookShelfItem
import com.google.gson.annotations.SerializedName

data class ResponseGetBookShelfBooks(
    @SerializedName("contents")
    val contents :List<BookShelfItem>,
    @SerializedName("pageMeta")
    val pageMeta : BookShelfMeta,
)
