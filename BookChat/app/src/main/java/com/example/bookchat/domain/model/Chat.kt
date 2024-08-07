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
)