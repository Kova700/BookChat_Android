package com.example.bookchat.data

data class ChatRoomListItem(
    val roomId: Long,
    val roomName: String,
    val roomSid: String,
    val roomMemberCount: Long,
    val defaultRoomImageType: Int,
    val roomImageUri: String,
    val lastChatId :Long,
    val lastActiveTime :String,
    val lastChatContent :String
)