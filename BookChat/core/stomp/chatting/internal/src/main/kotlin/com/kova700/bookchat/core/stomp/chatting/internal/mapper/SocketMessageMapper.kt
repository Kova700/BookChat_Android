package com.kova700.bookchat.core.stomp.chatting.internal.mapper

import com.kova700.bookchat.core.data.chat.external.model.Chat
import com.kova700.bookchat.core.data.user.external.model.User
import com.kova700.bookchat.core.stomp.chatting.external.model.CommonMessage
import com.kova700.bookchat.core.stomp.chatting.external.model.NotificationMessage
import com.kova700.bookchat.core.stomp.chatting.external.model.SocketMessage

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