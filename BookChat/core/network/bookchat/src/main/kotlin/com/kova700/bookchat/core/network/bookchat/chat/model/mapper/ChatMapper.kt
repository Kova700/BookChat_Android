package com.kova700.bookchat.core.network.bookchat.chat.model.mapper

import com.kova700.bookchat.core.data.chat.external.model.Chat
import com.kova700.bookchat.core.data.user.external.model.User
import com.kova700.bookchat.core.network.bookchat.chat.model.response.ChatResponse
import com.kova700.bookchat.core.network.bookchat.chat.model.response.RespondGetChat

fun ChatResponse.toChat(channelId: Long): Chat {
	return Chat(
		chatId = this.chatId,
		channelId = channelId,
		dispatchTime = this.dispatchTime,
		message = this.message,
		sender = senderId?.let {
			User.Default.copy(
				id = it
			)
		}
	)
}

fun RespondGetChat.toChat(): Chat {
	return Chat(
		chatId = chatId,
		channelId = channelId,
		message = message,
		dispatchTime = dispatchTime,
		sender = sender
	)
}
