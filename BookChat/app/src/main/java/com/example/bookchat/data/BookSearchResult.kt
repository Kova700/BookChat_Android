package com.example.bookchat.data

import com.example.bookchat.data.response.SearchingMeta
import com.google.gson.annotations.SerializedName


data class BookSearchResult(
    @SerializedName("bookResponses")
    val bookResponses :List<Book>,
    @SerializedName("meta")
    val searchingMeta : SearchingMeta
)
