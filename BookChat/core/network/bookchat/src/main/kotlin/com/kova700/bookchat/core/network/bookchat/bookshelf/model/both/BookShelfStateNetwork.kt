package com.kova700.bookchat.core.network.bookchat.bookshelf.model.both

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class BookShelfStateNetwork {
	@SerialName("WISH")
	WISH,

	@SerialName("READING")
	READING,

	@SerialName("COMPLETE")
	COMPLETE
}