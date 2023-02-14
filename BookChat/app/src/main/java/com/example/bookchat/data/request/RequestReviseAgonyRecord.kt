package com.example.bookchat.data.request

import com.google.gson.annotations.SerializedName

data class RequestReviseAgonyRecord(
    @SerializedName("recordTitle")
    val recordTitle :String,
    @SerializedName("recordContent")
    val recordContent :String
)
