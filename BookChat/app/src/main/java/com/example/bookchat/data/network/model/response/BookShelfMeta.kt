package com.example.bookchat.data.network.model.response

import com.google.gson.annotations.SerializedName

data class BookShelfMeta(
    @SerializedName("totalElements")
    val totalElements: Int,
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
