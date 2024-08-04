package com.example.bookchat.data.stomp.internal.mapper

import com.example.bookchat.data.database.model.ChatEntity
import com.example.bookchat.data.mapper.getChatType
import com.example.bookchat.data.stomp.external.model.CommonMessage
import com.example.bookchat.data.stomp.external.model.NotificationMessage
import com.example.bookchat.data.stomp.external.model.SocketMessage
import com.example.bookchat.domain.model.Chat
import com.example.bookchat.domain.model.ChatType
import com.example.bookchat.domain.model.User

fun SocketMessage.toChatEntity(chatRoomId: Long, clientId: Long): ChatEntity {
	return when (this) {
		is CommonMessage ->
			ChatEntity(
				chatId = chatId,
				chatRoomId = chatRoomId,
				senderId = senderId,
				message = message,
				chatType = getChatType(
					senderId = senderId,
					clientId = clientId
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
					clientId = clientId
				),
			)
	}
}

fun SocketMessage.toChat(
	channelId: Long,
	clientId: Long,
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
					clientId = clientId
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
				sender = null,
			)
	}
}