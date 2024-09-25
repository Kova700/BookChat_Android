package com.kova700.bookchat.core.network.bookchat.bookshelf.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BookShelfMeta(
	@SerialName("totalElements")
	val totalElements: Int,
	@SerialName("totalPages")
	val totalPages: Int,
	@SerialName("pageSize")
	val pageSize: Int,
	@SerialName("pageNumber")
	val pageNumber: Int,
	@SerialName("offset")
	val offset: Long,
	@SerialName("first")
	val first: Boolean,
	@SerialName("last")
	val last: Boolean,
	@SerialName("empty")
	val empty: Boolean,
)
