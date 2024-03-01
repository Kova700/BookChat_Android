package com.example.bookchat.data.mapper

import com.example.bookchat.data.database.model.ChatEntity
import com.example.bookchat.data.database.model.combined.ChatWithUser
import com.example.bookchat.data.response.ChatResponse
import com.example.bookchat.domain.model.Chat
import com.example.bookchat.domain.model.ChatStatus
import com.example.bookchat.domain.model.ChatType
import com.example.bookchat.domain.model.User

fun ChatResponse.toChatEntity(
	chatRoomId: Long,
	myUserId: Long
): ChatEntity {

	return ChatEntity(
		chatId = this.chatId,
		chatRoomId = chatRoomId,
		senderId = this.senderId,
		dispatchTime = this.dispatchTime,
		message = this.message,
		chatType = getChatType(
			senderId = senderId,
			myUserId = myUserId
		)
	)
}

fun ChatResponse.toChat(
	chatRoomId: Long,
	myUserId: Long
): Chat {

	return Chat(
		chatId = this.chatId,
		chatRoomId = chatRoomId,
		dispatchTime = this.dispatchTime,
		message = this.message,
		chatType = getChatType(
			senderId = senderId,
			myUserId = myUserId
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
		sender = null
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
	myUserId: Long
): ChatType {
	return when (senderId) {
		myUserId -> ChatType.Mine
		null -> ChatType.Notice
		else -> ChatType.Other
	}
}

fun List<ChatResponse>.toChatEntity(
	chatRoomId: Long,
	myUserId: Long
) = this.map {
	it.toChatEntity(
		chatRoomId = chatRoomId,
		myUserId = myUserId
	)
}

fun List<Chat>.toChatEntity() = this.map { it.toChatEntity() }