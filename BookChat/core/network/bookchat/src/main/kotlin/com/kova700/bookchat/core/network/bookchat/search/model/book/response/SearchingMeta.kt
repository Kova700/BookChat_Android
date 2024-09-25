package com.kova700.bookchat.core.network.bookchat.search.model.book.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SearchingMeta(
	@SerialName("is_end")
	val isEnd: Boolean,
	@SerialName("pageable_count")
	val pageableCount: Int,
	@SerialName("total_count")
	val totalCount: Int,
)