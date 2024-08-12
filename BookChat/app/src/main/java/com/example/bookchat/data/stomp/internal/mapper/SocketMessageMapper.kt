package com.example.bookchat.data.stomp.internal.mapper

import com.example.bookchat.data.database.model.ChatEntity
import com.example.bookchat.data.stomp.external.model.CommonMessage
import com.example.bookchat.data.stomp.external.model.NotificationMessage
import com.example.bookchat.data.stomp.external.model.SocketMessage
import com.example.bookchat.domain.model.Chat
import com.example.bookchat.domain.model.User

fun SocketMessage.toChatEntity(chatRoomId: Long, clientId: Long): ChatEntity {
	return when (this) {
		is CommonMessage ->
			ChatEntity(
				chatId = chatId,
				channelId = chatRoomId,
				senderId = senderId,
				message = message,
				dispatchTime = dispatchTime,
			)

		is NotificationMessage ->
			ChatEntity(
				chatId = chatId,
				channelId = chatRoomId,
				senderId = null,
				dispatchTime = dispatchTime,
				message = message,
			)
	}
}

fun SocketMessage.toChat(
	channelId: Long,
	sender: User? = null,
): Chat {
	return when (this) {
		is CommonMessage ->
			Chat(
				chatId = chatId,
				channelId = channelId,
				message = message,
				dispatchTime = dispatchTime,
				sender = sender,
			)

		is NotificationMessage ->
			Chat(
				chatId = chatId,
				channelId = channelId,
				dispatchTime = dispatchTime,
				message = message,
				sender = null,
			)
	}
}