package com.example.bookchat.data.mapper

import com.example.bookchat.data.database.model.ChatEntity
import com.example.bookchat.data.network.model.response.CommonMessage
import com.example.bookchat.data.network.model.response.NotificationMessage
import com.example.bookchat.data.network.model.response.SocketMessage
import com.example.bookchat.domain.model.Chat
import com.example.bookchat.domain.model.ChatType
import com.example.bookchat.domain.model.User

fun SocketMessage.toChatEntity(chatRoomId: Long, myUserId: Long): ChatEntity {
	return when (this) {
		is CommonMessage ->
			ChatEntity(
				chatId = chatId,
				chatRoomId = chatRoomId,
				senderId = senderId,
				message = message,
				chatType = getChatType(
					senderId = senderId,
					myUserId = myUserId
				),
				dispatchTime = dispatchTime,
			)

		is NotificationMessage ->
			ChatEntity(
				chatId = chatId,
				chatRoomId = chatRoomId,
				senderId = null,
				dispatchTime = dispatchTime,
				message = message,
				chatType = getChatType(
					senderId = null,
					myUserId = myUserId
				),
			)
	}
}

fun SocketMessage.toChat(
	channelId: Long,
	myUserId: Long,
	sender: User? = null,
): Chat {
	return when (this) {
		is CommonMessage ->
			Chat(
				chatId = chatId,
				chatRoomId = channelId,
				message = message,
				chatType = getChatType(
					senderId = senderId,
					myUserId = myUserId
				),
				dispatchTime = dispatchTime,
				sender = sender,
			)

		is NotificationMessage ->
			Chat(
				chatId = chatId,
				chatRoomId = channelId,
				dispatchTime = dispatchTime,
				message = message,
				chatType = ChatType.Notice,
				sender = null, //유저 정보 가져오기
			)
	}
}