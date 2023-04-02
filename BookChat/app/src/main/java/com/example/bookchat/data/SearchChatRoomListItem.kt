package com.example.bookchat.data

import com.google.gson.annotations.SerializedName

data class SearchChatRoomListItem(
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
    val roomImageUri: String,
    @SerializedName("tags")
    val tags: String,
    @SerializedName("lastChatId")
    val lastChatId: String,
    @SerializedName("lastActiveTime")
    val lastActiveTime: String,
)
