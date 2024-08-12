package com.example.bookchat.data.mapper

import com.example.bookchat.data.database.model.ChatEntity
import com.example.bookchat.data.database.model.combined.ChatWithUser
import com.example.bookchat.data.network.model.response.ChatResponse
import com.example.bookchat.data.network.model.response.RespondGetChat
import com.example.bookchat.domain.model.Chat
import com.example.bookchat.domain.model.ChatStatus
import com.example.bookchat.domain.model.User

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

fun ChatEntity.toChat(): Chat {
	return Chat(
		chatId = chatId,
		channelId = channelId,
		dispatchTime = dispatchTime,
		message = message,
		status = ChatStatus.getType(status)!!,
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

fun ChatWithUser.toChat(): Chat {
	return Chat(
		chatId = chatEntity.chatId,
		channelId = chatEntity.channelId,
		dispatchTime = chatEntity.dispatchTime,
		message = chatEntity.message,
		status = ChatStatus.getType(chatEntity.status)!!,
		sender = userEntity?.toUser()
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