package com.kova700.bookchat.core.database.chatting.external.chat.mapper

import com.kova700.bookchat.core.data.chat.external.model.Chat
import com.kova700.bookchat.core.data.chat.external.model.ChatState
import com.kova700.bookchat.core.data.user.external.model.User
import com.kova700.bookchat.core.database.chatting.external.chat.model.ChatEntity

fun ChatEntity.toChat(): Chat {
	return Chat(
		chatId = chatId,
		channelId = channelId,
		dispatchTime = dispatchTime,
		message = message,
		state = ChatState.getType(state)!!,
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
		state = state.code
	)
}

fun List<Chat>.toChatEntity() = this.map { it.toChatEntity() }