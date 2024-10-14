package com.kova700.bookchat.core.database.chatting.external.chat.mapper

import com.kova700.bookchat.core.data.chat.external.model.Chat
import com.kova700.bookchat.core.data.chat.external.model.ChatStatus
import com.kova700.bookchat.core.data.user.external.model.User
import com.kova700.bookchat.core.database.chatting.external.chat.model.ChatEntity

fun ChatEntity.toChat(): Chat {
	return Chat(
		chatId = chatId,
		channelId = channelId,
		dispatchTime = dispatchTime,
		message = message,
		status = ChatStatus.getType(status)!!,
		sender = senderId?.let { User.DEFAULT.copy(id = it) }
	)
}

fun Chat.toChatEntity(): ChatEntity {
	return ChatEntity(
		chatId = chatId,
		channelId = channelId,
		senderId = sender?.id,
		dispatchTime = dispatchTime,
		message = message,
		status = status.code
	)
}

fun List<Chat>.toChatEntity() = this.map { it.toChatEntity() }