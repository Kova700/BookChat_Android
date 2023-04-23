package com.example.bookchat.data

import com.example.bookchat.utils.UserDefaultProfileImageType
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class SearchChatRoomListItem(
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
    val bookAuthors: String,
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
    @SerializedName("roomImageUri")
    val roomImageUri: String?,
    @SerializedName("tags")
    val tags: String,
    @SerializedName("lastChatId")
    val lastChatId: String?,
    @SerializedName("lastActiveTime")
    val lastActiveTime: String?,
) : Serializable
