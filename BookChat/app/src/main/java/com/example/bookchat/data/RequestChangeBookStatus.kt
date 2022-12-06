package com.example.bookchat.data

import com.example.bookchat.utils.ReadingStatus
import com.example.bookchat.utils.StarRating
import com.google.gson.annotations.SerializedName

data class RequestChangeBookStatus(
    @SerializedName("readingStatus")
    private val readingStatus : ReadingStatus,
    @SerializedName("star")
    private val star :StarRating? = null,
    @SerializedName("pages")
    private val pages :Int? = null
)
