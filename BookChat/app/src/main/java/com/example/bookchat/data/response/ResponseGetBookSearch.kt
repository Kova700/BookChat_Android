package com.example.bookchat.data.response

import com.example.bookchat.data.Book
import com.google.gson.annotations.SerializedName


data class ResponseGetBookSearch(
    @SerializedName("bookResponses")
    val bookResponses :List<Book>,
    @SerializedName("meta")
    val searchingMeta : SearchingMeta
)
