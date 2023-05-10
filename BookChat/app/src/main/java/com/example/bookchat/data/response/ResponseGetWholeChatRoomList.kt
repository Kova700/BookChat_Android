package com.example.bookchat.data.response

import com.example.bookchat.data.WholeChatRoomListItem
import com.google.gson.annotations.SerializedName

data class ResponseGetWholeChatRoomList(
    @SerializedName("chatRoomResponseList")
    val chatRoomList :List<WholeChatRoomListItem>,
    @SerializedName("cursorMeta")
    val cursorMeta : CursorMeta
)
