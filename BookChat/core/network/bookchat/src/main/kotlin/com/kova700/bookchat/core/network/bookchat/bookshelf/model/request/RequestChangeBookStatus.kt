package com.kova700.bookchat.core.network.bookchat.bookshelf.model.request

import com.kova700.bookchat.core.data.bookshelf.external.model.BookShelfState
import com.kova700.bookchat.core.data.bookshelf.external.model.StarRating
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RequestChangeBookStatus(
	@SerialName("readingStatus")
	private val bookShelfState: BookShelfState,
	@SerialName("star")
	private val star: StarRating? = null,
	@SerialName("pages")
	private val pages: Int? = null,
)
