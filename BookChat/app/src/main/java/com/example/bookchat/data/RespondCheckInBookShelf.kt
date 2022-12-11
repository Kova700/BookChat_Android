package com.example.bookchat.data

import com.example.bookchat.utils.ReadingStatus
import com.google.gson.annotations.SerializedName

data class RespondCheckInBookShelf(
    @SerializedName("bookShelfId")
    val bookShelfId :Long,
    @SerializedName("bookId")
    val bookId :Long,
    @SerializedName("readingStatus")
    val readingStatus :ReadingStatus,
)
