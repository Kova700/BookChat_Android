package com.example.bookchat.data

import com.google.gson.annotations.SerializedName
import java.io.Serializable

//DTO
data class Book(
    @SerializedName("isbn")
    val isbn: String,
    @SerializedName("title")
    var title: String,
    @SerializedName("author")
    val authors: ArrayList<String>, //작가가 공동저자일 수도 있으니 배열로 선언
    @SerializedName("publisher")
    val publisher: String,
    @SerializedName("bookCoverImageUrl")
    val bookCoverImageUrl: String
) : Serializable
