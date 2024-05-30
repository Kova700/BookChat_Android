package com.example.bookchat.ui.channel.mapper.chat

import com.example.bookchat.domain.model.Chat
import com.example.bookchat.domain.model.ChatStatus
import com.example.bookchat.domain.model.ChatType
import com.example.bookchat.ui.channel.model.chat.ChatItem
import com.example.bookchat.utils.DateManager

//TODO : User -> Participant로 변경하고 거기에 유저의 채팅방 권한 넣기
fun List<Chat>.toChatItems(
	focusTargetId: Long?,
	isVisibleLastReadChatNotice: Boolean
): List<ChatItem> {
	val items = mutableListOf<ChatItem>()
	for (i in 0..lastIndex) {
		val chat = this[i]

		if (isVisibleLastReadChatNotice
			&& (chat.chatId == focusTargetId)
			&& ((i != 0) && (this[i - 1].status == ChatStatus.SUCCESS))
		) {
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
				)
			)

			ChatType.Other -> items.add(
				ChatItem.AnotherUser(
					chatId = chat.chatId,
					chatRoomId = chat.chatRoomId,
					message = chat.message,
					dispatchTime = chat.dispatchTime,
					sender = chat.sender,
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
		}

		if (chat.status != ChatStatus.SUCCESS) continue

		if (i == lastIndex) {
			items.add(
				ChatItem.DateSeparator(
					date = DateManager.getDateKoreanString(chat.dispatchTime)
				)
			)
			continue
		}

		val isSameDate = DateManager.isSameDate(
			dateTimeString = chat.dispatchTime,
			other = this[i + 1].dispatchTime
		)
		if (isSameDate.not()) {
			items.add(
				ChatItem.DateSeparator(
					date = DateManager.getDateKoreanString(chat.dispatchTime)
				)
			)
		}
	}

	return items
}