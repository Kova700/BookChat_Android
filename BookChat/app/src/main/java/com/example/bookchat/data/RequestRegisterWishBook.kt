package com.example.bookchat.data

import com.example.bookchat.utils.ReadingStatus
import com.google.gson.annotations.SerializedName

class RequestRegisterWishBook(book: Book){
    @SerializedName("isbn")
    private val isbn :String = book.isbn
    @SerializedName("title")
    private val title :String = book.title
    @SerializedName("authors")
    private val authors :List<String> = book.authors
    @SerializedName("publisher")
    private val publisher :String = book.publisher
    @SerializedName("bookCoverImageUrl")
    private val bookCoverImageUrl :String = book.bookCoverImageUrl
    @SerializedName("readingStatus")
    private val readingStatus :ReadingStatus = ReadingStatus.WISH
}