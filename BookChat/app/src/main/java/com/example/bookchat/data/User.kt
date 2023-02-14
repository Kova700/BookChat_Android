package com.example.bookchat.data

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("userNickname")
    val userNickname: String,
    @SerializedName("userEmail")
    val userEmail: String,
    @SerializedName("userProfileImageUri")
    val userProfileImageUri: String,
    @SerializedName("defaultProfileImageType")
    val defaultProfileImageType: Int
)
//구글은 그냥 Account 객체에 이메일 담아주네?
//카카오도 이렇게는 그냥 될텐데
//그럼 왜 카카오는 이렇게 안하고 Id토큰을 사용했을까,,?

