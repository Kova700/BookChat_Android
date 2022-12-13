package com.example.bookchat.data

import com.google.gson.annotations.SerializedName

data class BookReportRequest(
    @SerializedName("title")
    val title :String,
    @SerializedName("content")
    val content :String
)
