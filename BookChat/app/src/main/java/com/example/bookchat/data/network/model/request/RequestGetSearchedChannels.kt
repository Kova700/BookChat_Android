package com.example.bookchat.data.network.model.request

data class RequestGetSearchedChannels(
    var roomName: String? = null,
    var title: String? = null,
    var isbn: String? = null,
    var tags: String? = null,
)
