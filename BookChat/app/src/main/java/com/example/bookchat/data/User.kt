package com.example.bookchat.data

import com.example.bookchat.utils.UserDefaultProfileImageType
import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("userNickname")
    val userNickname: String,
    @SerializedName("userEmail")
    val userEmail: String,
    @SerializedName("userProfileImageUri")
    val userProfileImageUri: String?,
    @SerializedName("defaultProfileImageType")
    val defaultProfileImageType: UserDefaultProfileImageType
)