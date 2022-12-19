package com.example.bookchat.data

import com.example.bookchat.utils.AgonyFolderHexColor
import com.google.gson.annotations.SerializedName

data class Agony(
    @SerializedName("agonyId")
    val agonyId :Long,
    @SerializedName("title")
    val title :String,
    @SerializedName("hexColorCode")
    val hexColorCode : AgonyFolderHexColor
)
