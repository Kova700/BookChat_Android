package com.example.bookchat.ui.channel.model.chat

import com.example.bookchat.domain.model.ChatStatus
import com.example.bookchat.domain.model.User

sealed interface ChatItem {
	fun getCategoryId(): Long {
		return when (this) {
			is Message -> chatId
			is DateSeparator -> date.hashCode().toLong()
			is LastReadChatNotice -> hashCode().toLong()
		}
	}

	sealed class Message(
		open val chatId: Long,
		open val isFocused: Boolean
	) : ChatItem

	data class MyChat(
		override val chatId: Long,
		val chatRoomId: Long,
		val message: String,
		val status: ChatStatus = ChatStatus.SUCCESS,
		val dispatchTime: String,
		val sender: User?,
		override val isFocused: Boolean
	) : Message(chatId, isFocused)

	data class AnotherUser(
		override val chatId: Long,
		val chatRoomId: Long,
		val message: String,
		val dispatchTime: String,
		val sender: User?,
		override val isFocused: Boolean
	) : Message(chatId, isFocused)

	data class Notification(
		override val chatId: Long,
		val chatRoomId: Long,
		val message: String,
		val dispatchTime: String,
		override val isFocused: Boolean
	) : Message(chatId, isFocused)

	object LastReadChatNotice : ChatItem

	data class DateSeparator(
		val date: String,
	) : ChatItem

}