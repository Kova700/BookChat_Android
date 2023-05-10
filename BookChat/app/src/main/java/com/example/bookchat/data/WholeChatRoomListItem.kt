package com.example.bookchat.data

import com.example.bookchat.utils.UserDefaultProfileImageType
import com.google.gson.annotations.SerializedName
import java.io.Serializable

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
    val lastChatId: String? = null,
    @SerializedName("lastActiveTime")
    val lastActiveTime: String? = null,
) : Serializable {

    fun getBookAuthorsString() = bookAuthors.joinToString(",")

    fun getUserChatRoomListItem(): UserChatRoomListItem {
        return UserChatRoomListItem(
            roomId = roomId,
            roomName = roomName,
            roomSid = roomSid,
            roomMemberCount = roomMemberCount,
            defaultRoomImageType = defaultRoomImageType,
            roomImageUri = roomImageUri
        )
    }
}
