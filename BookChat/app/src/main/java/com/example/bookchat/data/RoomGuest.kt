package com.example.bookchat.data

import com.example.bookchat.utils.UserDefaultProfileImageType
import com.google.gson.annotations.SerializedName

data class RoomGuest(
    @SerializedName("id")
    val id: Long,
    @SerializedName("nickname")
    val nickname: String,
    @SerializedName("profileImageUrl")
    val profileImageUrl: String,
    @SerializedName("defaultProfileImageType")
    val defaultProfileImageType: UserDefaultProfileImageType
)
