package com.example.bookchat.domain.model

import com.example.bookchat.domain.model.ChatStatus.SUCCESS

data class Chat(
	val chatId: Long,
	val channelId: Long,
	val message: String,
	val status: ChatStatus = SUCCESS,
	val dispatchTime: String,
	val sender: User?,
) {
	fun getChatType(clientId: Long): ChatType {
		return when (sender?.id) {
			clientId -> ChatType.Mine
			null -> ChatType.Notice
			else -> ChatType.Other
		}
	}
}