package com.example.bookchat.data

import com.example.bookchat.utils.StarRating
import com.google.gson.annotations.SerializedName
import java.io.Serializable

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
    val publishAt: String,
    @SerializedName("star")
    var star: StarRating?,
    @SerializedName("singleLineAssessment")
    val singleLineAssessment: String?,
    @SerializedName("pages")
    var pages: Int,
    var isSwiped: Boolean = false,
) :Serializable{
    fun getBook() :Book{
        return Book(
            isbn = this.isbn,
            title = this.title,
            publishAt = this.publishAt,
            authors = this.authors,
            publisher = this.publisher,
            bookCoverImageUrl = this.bookCoverImageUrl
        )
    }

    fun setStarRating(value :Float){
        this.star = when(value){
            0F -> { StarRating.ZERO }; 0.5F -> { StarRating.HALF}
            1.0F -> { StarRating.ONE }; 1.5F -> { StarRating.ONE_HALF }
            2.0F -> { StarRating.TWO }; 2.5F -> { StarRating.TWO_HALF }
            3.0F -> { StarRating.THREE }; 3.5F -> { StarRating.THREE_HALF }
            4.0F -> { StarRating.FOUR }; 4.5F -> { StarRating.FOUR_HALF }
            5.0F -> { StarRating.FIVE }; else -> { StarRating.ZERO }
        }
    }

    fun copy() :BookShelfItem{
        return BookShelfItem(
            this.bookId, this.title, this.isbn,
            this.bookCoverImageUrl, this.authors.toList(),
            this.publisher, this.publishAt,
            this.star, this.singleLineAssessment,
            this.pages, this.isSwiped
        )
    }
}