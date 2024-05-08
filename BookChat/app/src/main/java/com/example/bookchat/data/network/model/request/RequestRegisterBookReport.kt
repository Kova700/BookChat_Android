package com.example.bookchat.data.network.model.request

import com.google.gson.annotations.SerializedName

data class RequestRegisterBookReport(
    @SerializedName("title")
    val title :String,
    @SerializedName("content")
    val content :String
)
