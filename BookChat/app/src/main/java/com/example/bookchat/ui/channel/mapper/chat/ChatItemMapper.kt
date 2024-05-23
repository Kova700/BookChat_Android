package com.example.bookchat.ui.channel.mapper.chat

import com.example.bookchat.domain.model.Chat
import com.example.bookchat.domain.model.ChatType
import com.example.bookchat.ui.channel.model.chat.ChatItem
import com.example.bookchat.utils.DateManager

fun List<Chat>.toChatItems(focusTargetId: Long?): List<ChatItem> {
	val items = mutableListOf<ChatItem>()
	for (i in 0..lastIndex) {
		val chat = this[i]

		if ((i != 0) && (chat.chatId == focusTargetId)) {
			items.add(ChatItem.LastReadChatNotice)
		}

		when (chat.chatType) {
			ChatType.Mine -> items.add(
				ChatItem.MyChat(
					chatId = chat.chatId,
					chatRoomId = chat.chatRoomId,
					message = chat.message,
					status = chat.status,
					dispatchTime = chat.dispatchTime,
					sender = chat.sender,
					isFocused = (i != 0) && (chat.chatId == focusTargetId)
				)
			)

			ChatType.Other -> items.add(
				ChatItem.AnotherUser(
					chatId = chat.chatId,
					chatRoomId = chat.chatRoomId,
					message = chat.message,
					dispatchTime = chat.dispatchTime,
					sender = chat.sender,
					isFocused = (i != 0) && (chat.chatId == focusTargetId)
				)
			)

			ChatType.Notice -> items.add(
				ChatItem.Notification(
					chatId = chat.chatId,
					chatRoomId = chat.chatRoomId,
					message = chat.message,
					dispatchTime = chat.dispatchTime,
					isFocused = (i != 0) && (chat.chatId == focusTargetId)
				)
			)
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