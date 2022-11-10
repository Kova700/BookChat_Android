package com.example.bookchat.data

import com.google.gson.annotations.SerializedName

data class BookShelfResult(
    @SerializedName("contents")
    val contents :List<BookShelfItem>,
    @SerializedName("pageMeta")
    val pageMeta :BookShelfMeta,
)
