package com.kova700.bookchat.core.network.bookchat.agonyrecord.model.request

import com.google.gson.annotations.SerializedName

data class RequestMakeAgonyRecord(
    @SerializedName("title")
    val title :String,
    @SerializedName("content")
    val content :String
)
