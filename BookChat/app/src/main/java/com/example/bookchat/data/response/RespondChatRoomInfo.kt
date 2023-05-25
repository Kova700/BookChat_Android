package com.example.bookchat.data.response

import com.example.bookchat.data.RoomGuest
import com.example.bookchat.data.RoomHost
import com.example.bookchat.data.RoomSubHost
import com.google.gson.annotations.SerializedName

data class RespondChatRoomInfo(
    @SerializedName("roomSize")
    val roomCapacity: Int,
    @SerializedName("roomTags")
    val roomTags: List<String>,
    @SerializedName("bookTitle")
    val bookTitle: String,
    @SerializedName("bookCoverImageUrl")
    val bookCoverImageUrl: String,
    @SerializedName("bookAuthors")
    val bookAuthors: List<String>,
    @SerializedName("roomHost")
    val roomHost: RoomHost,
    @SerializedName("roomSubHostList")
    val roomSubHostList: List<RoomSubHost>,
    @SerializedName("roomGuestList")
    val roomGuestList: List<RoomGuest>
)