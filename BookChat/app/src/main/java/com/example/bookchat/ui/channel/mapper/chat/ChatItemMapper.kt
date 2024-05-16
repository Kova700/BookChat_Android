package com.example.bookchat.ui.channel.mapper.chat

import com.example.bookchat.domain.model.Chat
import com.example.bookchat.domain.model.ChatType
import com.example.bookchat.ui.channel.model.chat.ChatItem
import com.example.bookchat.utils.DateManager

fun List<Chat>.toChatItem(): List<ChatItem> {
	val items = mutableListOf<ChatItem>()
	for (i in 0..lastIndex) {
		val chat = this[i]

		when (chat.chatType) {
			ChatType.Mine -> items.add(
				ChatItem.MyChat(
					chatId = chat.chatId,
					chatRoomId = chat.chatRoomId,
					message = chat.message,
					status = chat.status,
					dispatchTime = chat.dispatchTime,
					sender = chat.sender
				)
			)

			ChatType.Other -> items.add(
				ChatItem.AnotherUser(
					chatId = chat.chatId,
					chatRoomId = chat.chatRoomId,
					message = chat.message,
					dispatchTime = chat.dispatchTime,
					sender = chat.sender
				)
			)

			ChatType.Notice -> items.add(
				ChatItem.Notification(
					chatId = chat.chatId,
					chatRoomId = chat.chatRoomId,
					message = chat.message,
					dispatchTime = chat.dispatchTime,
				)
			)

			ChatType.UNKNOWN -> {} //TODO : 리팩토링해서 제거하기
		}

		if (i != lastIndex) {
			val isSameDate = DateManager.isSameDate(
				dateTimeString = chat.dispatchTime,
				other = this[i + 1].dispatchTime
			)
			if (isSameDate.not()) items.add(
				ChatItem.DateSeparator(
					date = DateManager.getDateKoreanString(chat.dispatchTime)
				)
			)
		}
	}

	return items
}