package com.example.bookchat.data

import com.example.bookchat.App
import com.example.bookchat.R
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
){
    fun getChatType() :Int{
        val myNickName = App.instance.getCachedUser().userNickname
        return when{
            this.senderNickname == myNickName -> R.layout.item_chatting_mine
            this.senderNickname == null -> R.layout.item_chatting_notice
            this.senderNickname != myNickName -> R.layout.item_chatting_other
            else -> 0
        }
    }
}