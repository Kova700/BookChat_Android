package com.example.bookchat.data.response

import com.example.bookchat.data.SearchChatRoomListItem
import com.google.gson.annotations.SerializedName

data class ResponseGetSearchChatRoomList(
    @SerializedName("chatRoomResponseList")
    val chatRoomList :List<SearchChatRoomListItem>,
    @SerializedName("cursorMeta")
    val cursorMeta : CursorMeta
)
