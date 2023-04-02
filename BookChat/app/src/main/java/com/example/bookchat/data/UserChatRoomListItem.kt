package com.example.bookchat.data

import com.google.gson.annotations.SerializedName

data class UserChatRoomListItem(
    @SerializedName("roomId")
    val roomId: Long,
    @SerializedName("roomName")
    val roomName: String,
    @SerializedName("roomSid")
    val roomSid: String,
    @SerializedName("roomMemberCount")
    val roomMemberCount: Long,
    @SerializedName("defaultRoomImageType")
    val defaultRoomImageType: Int,
    @SerializedName("roomImageUri")
    val roomImageUri: String?,
    @SerializedName("lastChatId")
    val lastChatId :Long,
    @SerializedName("lastActiveTime")
    val lastActiveTime :String,
    @SerializedName("lastChatContent")
    val lastChatContent :String
)