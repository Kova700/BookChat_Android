package com.example.bookchat.data

import com.google.gson.annotations.SerializedName
import java.io.Serializable

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
    val roomImageUri: String? = null,
    @SerializedName("lastChatId")
    val lastChatId: Long? = null,
    @SerializedName("lastActiveTime")
    val lastActiveTime: String? = null,
    @SerializedName("lastChatContent")
    val lastChatContent: String? = null
) : Serializable