package com.example.bookchat.utils

import com.google.gson.annotations.SerializedName

enum class ReadingStatus {
    @SerializedName("WISH") WISH,
    @SerializedName("READING") READING,
    @SerializedName("COMPLETE") COMPLETE
}