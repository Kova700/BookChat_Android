package com.kova700.bookchat.feature.channel.chatting.model

import com.kova700.bookchat.core.data.channel.external.model.ChannelMemberAuthority
import com.kova700.bookchat.core.data.chat.external.model.ChatState
import com.kova700.bookchat.core.data.user.external.model.User

sealed class ChatItem(
	open val isCaptureHeader: Boolean,
	open val isCaptureMiddle: Boolean,
	open val isCaptureBottom: Boolean,
) {
	fun getCategoryId(): Long {
		return when (this) {
			is Message -> chatId
			is DateSeparator -> date.hashCode().toLong()
			is LastReadChatNotice -> LAST_READ_ITEM_STABLE_ID
		}
	}

	sealed class Message(
		open val chatId: Long,
		open val message: String,
		override val isCaptureHeader: Boolean,
		override val isCaptureMiddle: Boolean,
		override val isCaptureBottom: Boolean,
	) : ChatItem(isCaptureHeader, isCaptureMiddle, isCaptureBottom)

	data class MyChat(
		override val chatId: Long,
		override val message: String,
		val channelId: Long,
		val state: ChatState,
		val dispatchTime: String,
		val sender: User?,
		override val isCaptureHeader: Boolean,
		override val isCaptureMiddle: Boolean,
		override val isCaptureBottom: Boolean,
	) : Message(chatId, message, isCaptureHeader, isCaptureMiddle, isCaptureBottom)

	data class AnotherUser(
		override val chatId: Long,
		override val message: String,
		val channelId: Long,
		val dispatchTime: String,
		val sender: User?,
		val authority: ChannelMemberAuthority,
		override val isCaptureHeader: Boolean,
		override val isCaptureMiddle: Boolean,
		override val isCaptureBottom: Boolean,
	) : Message(chatId, message, isCaptureHeader, isCaptureMiddle, isCaptureBottom) {
		val isTargetUserHost
			get() = authority == ChannelMemberAuthority.HOST

		val isTargetUserSubHost
			get() = authority == ChannelMemberAuthority.SUB_HOST
	}

	data class Notification(
		override val chatId: Long,
		override val message: String,
		val channelId: Long,
		val dispatchTime: String,
		override val isCaptureHeader: Boolean,
		override val isCaptureMiddle: Boolean,
		override val isCaptureBottom: Boolean,
	) : Message(chatId, message, isCaptureHeader, isCaptureMiddle, isCaptureBottom)

	data class LastReadChatNotice(
		override val isCaptureHeader: Boolean,
		override val isCaptureMiddle: Boolean,
		override val isCaptureBottom: Boolean,
	) : ChatItem(isCaptureHeader, isCaptureMiddle, isCaptureBottom)

	data class DateSeparator(
		val date: String,
		override val isCaptureHeader: Boolean,
		override val isCaptureMiddle: Boolean,
		override val isCaptureBottom: Boolean,
	) : ChatItem(isCaptureHeader, isCaptureMiddle, isCaptureBottom)

	companion object {
		const val LAST_READ_ITEM_STABLE_ID = -1L
	}
}