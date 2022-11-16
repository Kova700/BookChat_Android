package com.example.bookchat.data

import com.example.bookchat.utils.StarRating
import com.google.gson.annotations.SerializedName

data class BookShelfItem(
    @SerializedName("bookId")
    val bookId: Long,
    @SerializedName("title")
    val title: String,
    @SerializedName("isbn")
    val isbn: String,
    @SerializedName("bookCoverImageUrl")
    val bookCoverImageUrl: String,
    @SerializedName("authors")
    val authors: List<String>,
    @SerializedName("publisher")
    val publisher: String,
    @SerializedName("publishAt")
    val publishAt :String,
    @SerializedName("star")
    val star: StarRating,
    @SerializedName("singleLineAssessment")
    val singleLineAssessment: String,
    @SerializedName("pages")
    val pages: Int
)
