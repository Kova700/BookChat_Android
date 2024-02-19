package com.example.bookchat.data

import com.example.bookchat.data.local.entity.ChatRoomEntity
import com.example.bookchat.utils.UserDefaultProfileImageType
import com.google.gson.annotations.SerializedName
import java.io.Serializable

//TODO :
// 현재 채팅방 인원 수 뿐만 아니라 채팅방 정원 수도 추가
data class WholeChatRoomListItem(
    @SerializedName("roomId")
    val roomId: Long,
    @SerializedName("roomName")
    val roomName: String,
    @SerializedName("roomSid")
    val roomSid: String,
    @SerializedName("bookTitle")
    val bookTitle: String,
    @SerializedName("bookCoverImageUri")
    val bookCoverImageUri: String,
    @SerializedName("bookAuthors")
    val bookAuthors: List<String>,
    @SerializedName("hostName")
    val hostName: String,
    @SerializedName("hostDefaultProfileImageType")
    val hostDefaultProfileImageType: UserDefaultProfileImageType,
    @SerializedName("hostProfileImageUri")
    val hostProfileImageUri: String,
    @SerializedName("roomMemberCount")
    val roomMemberCount: Long,
    @SerializedName("defaultRoomImageType")
    val defaultRoomImageType: Int,
    @SerializedName("tags")
    val tags: String,
    @SerializedName("roomImageUri")
    val roomImageUri: String? = null,
    @SerializedName("lastChatId")
    val lastChatId: Long? = null,
    @SerializedName("lastActiveTime")
    val lastActiveTime: String? = null
) : Serializable {
    fun getBookAuthorsString() = bookAuthors.joinToString(",")

    fun toChatRoomEntity(): ChatRoomEntity {
        return ChatRoomEntity(
            roomId = roomId,
            roomName = roomName,
            roomSid = roomSid,
            roomMemberCount = roomMemberCount,
            defaultRoomImageType = defaultRoomImageType,
            roomImageUri = roomImageUri,
            lastChatId = lastChatId
        )
    }
}