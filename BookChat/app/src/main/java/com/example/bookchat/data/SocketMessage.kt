package com.example.bookchat.data

import com.example.bookchat.App
import com.example.bookchat.data.local.entity.ChatEntity
import com.example.bookchat.data.local.entity.ChatEntity.ChatType
import com.example.bookchat.utils.UserDefaultProfileImageType
import com.google.gson.annotations.SerializedName

sealed interface SocketMessage {

    fun toChatEntity(chatRoomId: Long): ChatEntity

    data class CommonMessage(
        @SerializedName("chatId")
        val chatId: Long,
        @SerializedName("senderId")
        val senderId: Long?,
        @SerializedName("senderNickname")
        val senderNickname: String?,
        @SerializedName("senderProfileImageUrl")
        val senderProfileImageUrl: String?,
        @SerializedName("senderDefaultProfileImageType")
        val senderDefaultProfileImageType: UserDefaultProfileImageType?,
        @SerializedName("dispatchTime")
        val dispatchTime: String,
        @SerializedName("message")
        val message: String,
        @SerializedName("messageType")
        val messageType: MessageType
    ) : SocketMessage {

        override fun toChatEntity(chatRoomId: Long): ChatEntity =
            ChatEntity(
                chatId = this.chatId,
                chatRoomId = chatRoomId,
                senderId = this.senderId,
                senderNickname = this.senderNickname,
                senderProfileImageUrl = this.senderProfileImageUrl,
                senderDefaultProfileImageType = this.senderDefaultProfileImageType,
                dispatchTime = this.dispatchTime,
                message = this.message,
                chatType = getChatType()
            )

        private fun getChatType(): ChatType {
            val myId = App.instance.getCachedUser().userId

            return when (this.senderId) {
                myId -> ChatType.Mine
                null -> ChatType.Notice
                else -> ChatType.Other
            }
        }
    }

    data class NotificationMessage(
        @SerializedName("targetId")
        val targetId: Long,
        @SerializedName("chatId")
        val chatId: Long,
        @SerializedName("message")
        val message: String,
        @SerializedName("dispatchTime")
        val dispatchTime: String,
        @SerializedName("messageType")
        val messageType: MessageType
    ) : SocketMessage {
        override fun toChatEntity(chatRoomId: Long): ChatEntity =
            ChatEntity(
                chatId = this.chatId,
                chatRoomId = chatRoomId,
                senderId = null,
                senderNickname = null,
                senderProfileImageUrl = null,
                senderDefaultProfileImageType = null,
                dispatchTime = this.dispatchTime,
                message = this.message,
                chatType = ChatType.Notice
            )
    }
}

enum class MessageType {
    CHAT, ENTER, EXIT,
    NOTICE_HOST_DELEGATE,
    NOTICE_KICK,
    NOTICE_SUB_HOST_DISMISS,
    NOTICE_SUB_HOST_DELEGATE
}