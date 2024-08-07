package com.example.bookchat.data.mapper

import com.example.bookchat.data.database.model.ChatEntity
import com.example.bookchat.data.database.model.combined.ChatWithUser
import com.example.bookchat.data.network.model.response.ChatResponse
import com.example.bookchat.data.network.model.response.RespondGetChat
import com.example.bookchat.domain.model.Chat
import com.example.bookchat.domain.model.ChatStatus
import com.example.bookchat.domain.model.ChatType
import com.example.bookchat.domain.model.User

fun ChatResponse.toChatEntity(
	chatRoomId: Long,
	clientId: Long
): ChatEntity {

	return ChatEntity(
		chatId = this.chatId,
		chatRoomId = chatRoomId,
		senderId = this.senderId,
		dispatchTime = this.dispatchTime,
		message = this.message,
		chatType = getChatType(
			senderId = senderId,
			clientId = clientId
		)
	)
}

fun ChatResponse.toChat(
	chatRoomId: Long,
	clientId: Long
): Chat {

	return Chat(
		chatId = this.chatId,
		chatRoomId = chatRoomId,
		dispatchTime = this.dispatchTime,
		message = this.message,
		chatType = getChatType(
			senderId = senderId,
			clientId = clientId
		),
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
		chatRoomId = chatRoomId,
		dispatchTime = dispatchTime,
		message = message,
		chatType = chatType,
		status = ChatStatus.getType(status)!!,
		sender = senderId?.let {
			User.Default.copy(
				id = it
			)
		}
	)
}

fun RespondGetChat.toChat(clientId: Long): Chat {
	return Chat(
		chatId = chatId,
		chatRoomId = channelId,
		chatType = getChatType(
			senderId = sender.id,
			clientId = clientId
		),
		message = message,
		dispatchTime = dispatchTime,
		sender = sender
	)
}

fun ChatWithUser.toChat(): Chat {
	return Chat(
		chatId = chatEntity.chatId,
		chatRoomId = chatEntity.chatRoomId,
		dispatchTime = chatEntity.dispatchTime,
		message = chatEntity.message,
		chatType = chatEntity.chatType,
		status = ChatStatus.getType(chatEntity.status)!!,
		sender = userEntity?.toUser()
	)
}

fun Chat.toChatEntity(): ChatEntity {
	return ChatEntity(
		chatId = chatId,
		chatRoomId = chatRoomId,
		senderId = sender?.id,
		dispatchTime = dispatchTime,
		message = message,
		chatType = chatType,
		status = status.code
	)
}

fun getChatType(
	senderId: Long?,
	clientId: Long
): ChatType {
	return when (senderId) {
		clientId -> ChatType.Mine
		null -> ChatType.Notice
		else -> ChatType.Other
	}
}

fun List<ChatResponse>.toChatEntity(
	chatRoomId: Long,
	clientId: Long
) = this.map {
	it.toChatEntity(
		chatRoomId = chatRoomId,
		clientId = clientId
	)
}

fun List<Chat>.toChatEntity() = this.map { it.toChatEntity() }