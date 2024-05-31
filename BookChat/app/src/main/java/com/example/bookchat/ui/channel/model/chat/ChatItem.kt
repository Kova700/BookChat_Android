package com.example.bookchat.ui.channel.model.chat

import com.example.bookchat.domain.model.ChannelMemberAuthority
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
	) : ChatItem

	data class MyChat(
		override val chatId: Long,
		val chatRoomId: Long,
		val message: String,
		val status: ChatStatus,
		val dispatchTime: String,
		val sender: User?,
	) : Message(chatId)

	data class AnotherUser(
		override val chatId: Long,
		val chatRoomId: Long,
		val message: String,
		val dispatchTime: String,
		val sender: User?,
		val authority: ChannelMemberAuthority,
	) : Message(chatId) {
		val isTargetUserHost
			get() = authority == ChannelMemberAuthority.HOST

		val isTargetUserSubHost
			get() = authority == ChannelMemberAuthority.SUB_HOST
	}

	data class Notification(
		override val chatId: Long,
		val chatRoomId: Long,
		val message: String,
		val dispatchTime: String,
	) : Message(chatId)

	object LastReadChatNotice : ChatItem

	data class DateSeparator(
		val date: String,
	) : ChatItem

}