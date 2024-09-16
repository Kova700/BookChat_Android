package com.kova700.bookchat.core.network.bookchat.bookshelf.model.request

import com.google.gson.annotations.SerializedName
import com.kova700.bookchat.core.data.bookshelf.external.model.BookShelfState
import com.kova700.bookchat.core.data.bookshelf.external.model.StarRating

data class RequestChangeBookStatus(
	@SerializedName("readingStatus")
	private val bookShelfState: BookShelfState,
	@SerializedName("star")
	private val star: StarRating? = null,
	@SerializedName("pages")
	private val pages: Int? = null,
)
