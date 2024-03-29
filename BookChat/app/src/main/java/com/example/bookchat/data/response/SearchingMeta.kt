package com.example.bookchat.data.response

import com.google.gson.annotations.SerializedName

data class SearchingMeta (
    @SerializedName("is_end")
    val isEnd :Boolean,
    @SerializedName("pageable_count")
    val pageableCount :Int,
    @SerializedName("total_count")
    val totalCount :Int
)