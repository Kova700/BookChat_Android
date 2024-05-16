package com.example.bookchat.ui.channel.model.chat

import com.example.bookchat.domain.model.ChatStatus
import com.example.bookchat.domain.model.User

sealed interface ChatItem {
	fun getCategoryId(): Long {
		return when (this) {
			is MyChat -> chatId
			is AnotherUser -> chatId
			is Notification -> chatId
			is DateSeparator -> date.hashCode().toLong()
		}
	}

	data class MyChat(
		val chatId: Long,
		val chatRoomId: Long,
		val message: String,
		val status: ChatStatus = ChatStatus.SUCCESS,
		val dispatchTime: String,
		val sender: User?
	) : ChatItem

	data class AnotherUser(
		val chatId: Long,
		val chatRoomId: Long,
		val message: String,
		val dispatchTime: String,
		val sender: User?
	) : ChatItem

	data class Notification(
		val chatId: Long,
		val chatRoomId: Long,
		val message: String,
		val dispatchTime: String,
	) : ChatItem

	data class DateSeparator(
		val date: String,
	) : ChatItem

}