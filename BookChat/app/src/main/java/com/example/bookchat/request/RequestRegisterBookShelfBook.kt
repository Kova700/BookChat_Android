package com.example.bookchat.request

import com.example.bookchat.data.Book
import com.example.bookchat.utils.ReadingStatus
import com.google.gson.annotations.SerializedName

class RequestRegisterBookShelfBook(
    @SerializedName("bookRequest")
    private val book: Book,
    @SerializedName("readingStatus")
    private val readingStatus :ReadingStatus,
    @SerializedName("star")
    private val star : String? = null
)