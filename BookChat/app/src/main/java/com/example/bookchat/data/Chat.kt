package com.example.bookchat.data

import com.example.bookchat.App
import com.example.bookchat.data.local.entity.ChatEntity
import com.example.bookchat.data.local.entity.ChatEntity.ChatType
import com.example.bookchat.utils.UserDefaultProfileImageType
import com.google.gson.annotations.SerializedName

data class Chat(
    @SerializedName("chatId")
    val chatId: Long,
    @SerializedName("senderId")
    val senderId: Long?,
    @SerializedName("dispatchTime")
    val dispatchTime: String,
    @SerializedName("message")
    val message: String
) {
    private fun getChatType(): ChatType {
        val myId = App.instance.getCachedUser().userId

        return when (this.senderId) {
            myId -> ChatType.Mine
            null -> ChatType.Notice
            else -> ChatType.Other
        }
    }

    fun toChatEntity(chatRoomId: Long) =
        ChatEntity(
            chatId = this.chatId,
            chatRoomId = chatRoomId,
            senderId = this.senderId,
            dispatchTime = this.dispatchTime,
            message = this.message,
            chatType = getChatType()
        )
}