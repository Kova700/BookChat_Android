package com.example.bookchat.domain.model

import com.example.bookchat.domain.model.ChatStatus.SUCCESS

data class Chat(
	val chatId: Long,
	val chatRoomId: Long,
	val message: String,
	val chatType: ChatType,
	val status: ChatStatus = SUCCESS,
	val dispatchTime: String,
	val sender: User?
) {
	companion object {
		val DEFAULT = Chat(
			chatId = 0L,
			chatRoomId = 0L,
			message = "",
			chatType = ChatType.UNKNOWN,
			dispatchTime = "",
			sender = null,
		)
	}
}