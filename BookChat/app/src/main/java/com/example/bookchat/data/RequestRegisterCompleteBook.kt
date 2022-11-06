package com.example.bookchat.data

import com.example.bookchat.utils.ReadingStatus
import com.google.gson.annotations.SerializedName

data class RequestRegisterCompleteBook(
    @SerializedName("isbn")
    val isbn :String,
    @SerializedName("title")
    val title :String,
    @SerializedName("authors")
    val authors :List<String>,
    @SerializedName("publisher")
    val publisher :String,
    @SerializedName("bookCoverImageUrl")
    val bookCoverImageUrl :String,
    @SerializedName("star")
    val star : String,
    @SerializedName("singleLineAssessment")
    val singleLineAssessment :String,
    @SerializedName("readingStatus")
    val readingStatus : ReadingStatus = ReadingStatus.COMPLETE,
)
