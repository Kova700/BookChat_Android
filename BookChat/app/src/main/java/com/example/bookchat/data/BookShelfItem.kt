package com.example.bookchat.data

import com.example.bookchat.utils.StarRating
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class BookShelfItem(
    @SerializedName("bookShelfId")
    val bookShelfId: Long,
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
    @SerializedName("pages")
    var pages: Int,
    @SerializedName("star")
    var star: StarRating?,
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

    fun getBookShelfDataItem() = BookShelfDataItem(this)
}

data class BookShelfDataItem(
    val bookShelfItem :BookShelfItem,
    var isSwiped: Boolean = false
) :Serializable