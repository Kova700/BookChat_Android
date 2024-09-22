package com.kova700.bookchat.core.network.bookchat.bookshelf.model.request

import com.kova700.bookchat.core.data.bookshelf.external.model.BookShelfState
import com.kova700.bookchat.core.data.bookshelf.external.model.StarRating
import com.kova700.bookchat.core.network.bookchat.common.model.BookRequest
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class RequestRegisterBookShelfBook(
	@SerialName("bookRequest")
	private val bookRequest: BookRequest,
	@SerialName("readingStatus")
	private val bookShelfState: BookShelfState,
	@SerialName("star")
	private val star: StarRating? = null,
)