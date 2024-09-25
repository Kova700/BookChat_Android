package com.kova700.bookchat.core.network.bookchat.bookshelf.model.response

import com.kova700.bookchat.core.data.bookshelf.external.model.BookShelfState
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BookStateInBookShelfResponse(
	@SerialName("bookShelfId")
	val bookShelfId: Long,
	@SerialName("readingStatus")
	val bookShelfState: BookShelfState,
)
