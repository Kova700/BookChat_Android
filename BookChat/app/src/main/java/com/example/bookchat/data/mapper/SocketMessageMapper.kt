package com.example.bookchat.data.mapper

import com.example.bookchat.data.SocketMessage
import com.example.bookchat.data.database.model.ChatEntity

fun SocketMessage.toChatEntity(chatRoomId: Long, myUserId: Long): ChatEntity {
	return when (this) {
		is SocketMessage.CommonMessage ->
			ChatEntity(
				chatId = this.chatId,
				chatRoomId = chatRoomId,
				senderId = this.senderId,
				message = this.message,
				chatType = getChatType(
					senderId = senderId,
					myUserId = myUserId
				),
				dispatchTime = this.dispatchTime,
			)

		is SocketMessage.NotificationMessage ->
			ChatEntity(
				chatId = this.chatId,
				chatRoomId = chatRoomId,
				senderId = null,
				dispatchTime = this.dispatchTime,
				message = this.message,
				chatType = getChatType(
					senderId = null,
					myUserId = myUserId
				),
			)
	}
}