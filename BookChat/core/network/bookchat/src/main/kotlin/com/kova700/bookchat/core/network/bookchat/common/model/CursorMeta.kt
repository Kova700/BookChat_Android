package com.kova700.bookchat.core.network.bookchat.common.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CursorMeta(
	@SerialName("sliceSize")
	val sliceSize: Int,
	@SerialName("contentSize")
	val contentSize: Int,
	@SerialName("hasContent")
	val hasContent: Boolean,
	@SerialName("hasNext")
	val hasNext: Boolean,
	@SerialName("last")
	val last: Boolean,
	@SerialName("first")
	val first: Boolean,
	@SerialName("nextCursorId")
	val nextCursorId: Long?,
)
