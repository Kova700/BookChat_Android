package com.example.bookchat.data.mapper

import com.example.bookchat.data.Chat
import com.example.bookchat.data.database.model.ChatEntity

fun Chat.toChatEntity(
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

fun getChatType(
	senderId: Long?,
	myUserId: Long
): ChatEntity.ChatType {
	return when (senderId) {
		myUserId -> ChatEntity.ChatType.Mine
		null -> ChatEntity.ChatType.Notice
		else -> ChatEntity.ChatType.Other
	}
}

fun List<Chat>.toChatEntity(
	chatRoomId: Long,
	myUserId: Long
) = this.map {
	it.toChatEntity(
		chatRoomId = chatRoomId,
		myUserId = myUserId
	)
}
