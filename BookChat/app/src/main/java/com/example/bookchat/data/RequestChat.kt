package com.example.bookchat.data

import com.google.gson.annotations.SerializedName

data class RequestChat(
    @SerializedName("message")
    val message :String
)