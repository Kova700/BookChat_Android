package com.example.bookchat.data.request

data class RequestGetWholeChatRoomList(
    val postCursorId: Long?,
    val size: Int,
    var roomName: String? = null,
    var title: String? = null,
    var isbn: String? = null,
    var tags: String? = null,
)
