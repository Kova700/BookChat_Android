package com.example.bookchat.data

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Book(
    @SerializedName("isbn")
    val isbn: String,
    @SerializedName("title")
    var title: String,
    @SerializedName("datetime")
    var datetime :String,
    @SerializedName("author")
    val authors: List<String>,
    @SerializedName("publisher")
    val publisher: String,
    @SerializedName("bookCoverImageUrl")
    val bookCoverImageUrl: String
) : Serializable
