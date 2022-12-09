package com.example.bookchat.data

import com.example.bookchat.utils.ReadingStatus
import com.google.gson.annotations.SerializedName

data class RespondCheckInBookShelf(
    @SerializedName("bookShelfId")
    private val bookShelfId :Long,
    @SerializedName("bookId")
    private val bookId :Long,
    @SerializedName("readingStatus")
    private val readingStatus :ReadingStatus,
)
