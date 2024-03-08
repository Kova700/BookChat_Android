package com.example.bookchat.data.request

import com.example.bookchat.domain.model.BookShelfState
import com.example.bookchat.domain.model.StarRating
import com.google.gson.annotations.SerializedName

class RequestRegisterBookShelfBook(
	@SerializedName("bookRequest")
	private val bookRequest: BookRequest,
	@SerializedName("readingStatus")
	private val bookShelfState: BookShelfState,
	@SerializedName("star")
	private val star: StarRating? = null
)