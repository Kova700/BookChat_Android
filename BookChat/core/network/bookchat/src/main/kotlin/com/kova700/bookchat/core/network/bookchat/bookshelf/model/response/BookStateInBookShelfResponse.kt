package com.kova700.bookchat.core.network.bookchat.bookshelf.model.response

import com.google.gson.annotations.SerializedName
import com.kova700.bookchat.core.data.bookshelf.external.model.BookShelfState

data class BookStateInBookShelfResponse(
	@SerializedName("bookShelfId")
	val bookShelfId: Long,
	@SerializedName("bookId")
	val bookId: Long,
	@SerializedName("readingStatus")
	val bookShelfState: BookShelfState,
)
