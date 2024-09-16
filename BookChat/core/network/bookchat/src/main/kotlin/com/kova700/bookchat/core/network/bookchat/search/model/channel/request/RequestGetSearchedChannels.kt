package com.kova700.bookchat.core.network.bookchat.search.model.channel.request

data class RequestGetSearchedChannels(
	val roomName: String? = null,
	val title: String? = null,
	val isbn: String? = null,
	val tags: String? = null,
)
