package com.example.bookchat.data

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Token(
    @SerializedName("accessToken")
    var accessToken :String,
    @SerializedName("refreshToken")
    val refreshToken :String
) : Serializable
