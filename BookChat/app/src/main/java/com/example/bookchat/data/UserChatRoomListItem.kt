package com.example.bookchat.data

import com.example.bookchat.data.local.entity.ChatRoomEntity
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
) : Serializable {

    fun toChatRoomEntity() =
        ChatRoomEntity(
            roomId = roomId,
            roomName = roomName,
            roomSid = roomSid,
            roomMemberCount = roomMemberCount,
            defaultRoomImageType = defaultRoomImageType,
            roomImageUri = roomImageUri,
            lastChatId = lastChatId,
            lastActiveTime = lastActiveTime,
            lastChatContent = lastChatContent
        )
}