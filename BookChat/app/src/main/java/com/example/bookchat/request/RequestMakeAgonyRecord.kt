package com.example.bookchat.request

import com.google.gson.annotations.SerializedName

data class RequestMakeAgonyRecord(
    @SerializedName("title")
    val title :String,
    @SerializedName("content")
    val content :String
)
