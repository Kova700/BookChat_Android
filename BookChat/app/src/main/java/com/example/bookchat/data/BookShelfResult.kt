package com.example.bookchat.data

import com.google.gson.annotations.SerializedName

data class BookShelfResult(
    @SerializedName("contents")
    val contents :List<BookShelfItem>,

    @SerializedName("totalElements")
    val totalElements: Long,
    @SerializedName("totalPages")
    val totalPages: Int,
    @SerializedName("pageSize")
    val pageSize: Int,
    @SerializedName("pageNumber")
    val pageNumber: Int,
    @SerializedName("offset")
    val offset: Long,
    @SerializedName("first")
    val first: Boolean,
    @SerializedName("last")
    val last: Boolean,
    @SerializedName("empty")
    val empty: Boolean
)
