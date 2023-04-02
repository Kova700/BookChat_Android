package com.example.bookchat.data.response

import com.example.bookchat.data.UserChatRoomListItem
import com.google.gson.annotations.SerializedName

data class ResponseGetUserChatRoomList(
    @SerializedName("userChatRoomResponseList")
    val chatRoomList :List<UserChatRoomListItem>,
    @SerializedName("cursorMeta")
    val cursorMeta : CursorMeta
)