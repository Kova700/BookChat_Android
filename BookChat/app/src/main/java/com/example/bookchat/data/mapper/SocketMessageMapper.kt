package com.example.bookchat.data.mapper

import com.example.bookchat.data.SocketMessage
import com.example.bookchat.data.database.model.ChatEntity
import com.example.bookchat.domain.model.Chat
import com.example.bookchat.domain.model.ChatType
import com.example.bookchat.domain.model.User

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

fun SocketMessage.toChat(
	chatRoomId: Long,
	myUserId: Long,
	sender: User? = null,
): Chat {
	return when (this) {
		is SocketMessage.CommonMessage ->
			Chat(
				chatId = this.chatId,
				chatRoomId = chatRoomId,
				message = this.message,
				chatType = getChatType(
					senderId = senderId,
					myUserId = myUserId
				),
				dispatchTime = this.dispatchTime,
				sender = sender,
			)

		is SocketMessage.NotificationMessage ->
			Chat(
				chatId = this.chatId,
				chatRoomId = chatRoomId,
				dispatchTime = this.dispatchTime,
				message = this.message,
				chatType = ChatType.Notice,
				sender = null, //유저 정보 가져오기
			)
	}
}