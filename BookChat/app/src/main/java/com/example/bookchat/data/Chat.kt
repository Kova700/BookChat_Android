package com.example.bookchat.data

import com.example.bookchat.utils.UserDefaultProfileImageType
import com.google.gson.annotations.SerializedName

data class Chat(
    @SerializedName("chatId")
    val chatId :Long,
    @SerializedName("senderId")
    val senderId :Long?,
    @SerializedName("senderNickname")
    val senderNickname: String?,
    @SerializedName("senderProfileImageUrl")
    val senderProfileImageUrl :String?,
    @SerializedName("senderDefaultProfileImageType")
    val senderDefaultProfileImageType : UserDefaultProfileImageType,
    @SerializedName("dispatchTime")
    val dispatchTime :String,
    @SerializedName("message")
    val message :String
)