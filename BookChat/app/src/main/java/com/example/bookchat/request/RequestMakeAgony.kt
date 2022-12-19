package com.example.bookchat.request

import com.example.bookchat.utils.AgonyFolderHexColor
import com.google.gson.annotations.SerializedName

data class RequestMakeAgony(
    @SerializedName("title")
    val title :String,
    @SerializedName("hexColorCode")
    val hexColorCode :AgonyFolderHexColor
)
