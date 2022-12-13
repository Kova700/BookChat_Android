package com.example.bookchat.data

import com.google.gson.annotations.SerializedName

data class BookReport(
    @SerializedName("reportTitle")
    var reportTitle :String,
    @SerializedName("reportContent")
    var reportContent :String,
    @SerializedName("reportCreatedAt")
    val reportCreatedAt :String? = null
)
