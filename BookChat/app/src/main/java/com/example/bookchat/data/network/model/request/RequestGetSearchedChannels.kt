package com.example.bookchat.data.network.model.request

data class RequestGetSearchedChannels(
	val roomName: String? = null,
	val title: String? = null,
	val isbn: String? = null,
	val tags: String? = null,
)
