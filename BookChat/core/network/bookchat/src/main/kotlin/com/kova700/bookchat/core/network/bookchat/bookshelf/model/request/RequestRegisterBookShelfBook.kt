package com.kova700.bookchat.core.network.bookchat.bookshelf.model.request

import com.google.gson.annotations.SerializedName
import com.kova700.bookchat.core.data.bookshelf.external.model.BookShelfState
import com.kova700.bookchat.core.data.bookshelf.external.model.StarRating
import com.kova700.bookchat.core.network.bookchat.common.model.BookRequest

class RequestRegisterBookShelfBook(
	@SerializedName("bookRequest")
	private val bookRequest: BookRequest,
	@SerializedName("readingStatus")
	private val bookShelfState: BookShelfState,
	@SerializedName("star")
	private val star: StarRating? = null,
)