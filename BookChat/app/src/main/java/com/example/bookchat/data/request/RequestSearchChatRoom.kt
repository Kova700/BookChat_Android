package com.example.bookchat.data.request

data class RequestSearchChatRoom(
    val postCursorId: Int?,
    val size: String?,
    var roomName: String? = null,
    var title: String? = null,
    var isbn: String? = null,
    var tags: String? = null,
)
