package com.example.bookchat.data.response

import com.example.bookchat.domain.model.BookShelfState
import com.google.gson.annotations.SerializedName

data class BookStateInBookShelfResponse(
	@SerializedName("bookShelfId")
    val bookShelfId :Long,
	@SerializedName("bookId")
    val bookId :Long,
	@SerializedName("readingStatus")
    val bookShelfState : BookShelfState,
)
