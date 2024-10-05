package com.kova700.bookchat.feature.channel.chatting.mapper

import android.util.Log
import com.kova700.bookchat.core.data.channel.external.model.Channel
import com.kova700.bookchat.core.data.channel.external.model.ChannelMemberAuthority
import com.kova700.bookchat.core.data.chat.external.model.Chat
import com.kova700.bookchat.core.data.chat.external.model.ChatStatus
import com.kova700.bookchat.core.data.chat.external.model.ChatType
import com.kova700.bookchat.feature.channel.chatting.model.ChatItem
import com.kova700.bookchat.util.Constants.TAG
import com.kova700.bookchat.util.date.isSameDate
import com.kova700.bookchat.util.date.toDateKoreanString

fun List<Chat>.toChatItems(
	channel: Channel?,
	clientId: Long,
	captureHeaderItemId: Long?,
	captureBottomItemId: Long?,
	focusTargetId: Long?,
	isVisibleLastReadChatNotice: Boolean,
): List<ChatItem> {

	val items = mutableListOf<ChatItem>()
	var isFindCaptureBottom = false
	var isFindCaptureHeader = false

	for (i in 0..lastIndex) {
		val chat = this[i]
		Log.d(TAG, ": toChatItems() - chat :$chat")

		if (isVisibleLastReadChatNotice
			&& (chat.chatId == focusTargetId)
			&& ((i != 0) && (this[i - 1].status == ChatStatus.SUCCESS))
		) {
			val isCaptureBottom = captureBottomItemId == ChatItem.LAST_READ_ITEM_STABLE_ID
			val isCaptureHeader = captureHeaderItemId == ChatItem.LAST_READ_ITEM_STABLE_ID
			isFindCaptureBottom = isFindCaptureBottom || isCaptureBottom
			isFindCaptureHeader = isFindCaptureHeader || isCaptureHeader
			items.add(
				ChatItem.LastReadChatNotice(
					isCaptureBottom = isCaptureBottom,
					isCaptureMiddle = isFindCaptureBottom
									&& isCaptureBottom.not()
									&& isCaptureHeader.not()
									&& isFindCaptureHeader.not(),
					isCaptureHeader = isCaptureHeader,
				)
			)
		}
		val isCaptureBottom = captureBottomItemId == chat.chatId
		val isCaptureHeader = captureHeaderItemId == chat.chatId
		isFindCaptureBottom = isFindCaptureBottom || isCaptureBottom
		isFindCaptureHeader = isFindCaptureHeader || isCaptureHeader

		when (chat.getChatType(clientId)) {
			ChatType.Mine -> items.add(
				ChatItem.MyChat(
					chatId = chat.chatId,
					channelId = chat.channelId,
					message = chat.message,
					status = chat.status,
					dispatchTime = chat.dispatchTime,
					sender = chat.sender,
					isCaptureBottom = isCaptureBottom,
					isCaptureMiddle = isFindCaptureBottom
									&& isCaptureBottom.not()
									&& isCaptureHeader.not()
									&& isFindCaptureHeader.not(),
					isCaptureHeader = isCaptureHeader,
				)
			)

			ChatType.Other -> items.add(
				ChatItem.AnotherUser(
					chatId = chat.chatId,
					channelId = chat.channelId,
					message = chat.message,
					dispatchTime = chat.dispatchTime,
					sender = chat.sender,
					authority = channel?.participantAuthorities?.get(chat.sender?.id)
						?: ChannelMemberAuthority.GUEST,
					isCaptureBottom = isCaptureBottom,
					isCaptureMiddle = isFindCaptureBottom
									&& isCaptureBottom.not()
									&& isCaptureHeader.not()
									&& isFindCaptureHeader.not(),
					isCaptureHeader = isCaptureHeader,
				)
			)

			ChatType.Notice -> items.add(
				ChatItem.Notification(
					chatId = chat.chatId,
					channelId = chat.channelId,
					message = chat.message,
					dispatchTime = chat.dispatchTime,
					isCaptureBottom = isCaptureBottom,
					isCaptureMiddle = isFindCaptureBottom
									&& isCaptureBottom.not()
									&& isCaptureHeader.not()
									&& isFindCaptureHeader.not(),
					isCaptureHeader = isCaptureHeader,
				)
			)
		}

		if (chat.status != ChatStatus.SUCCESS) continue

		if (i == lastIndex) {
			val date = chat.dispatchTime.toDateKoreanString()
			val isCaptureBottom = captureBottomItemId == date.hashCode().toLong()
			val isCaptureHeader = captureHeaderItemId == date.hashCode().toLong()
			isFindCaptureBottom = isFindCaptureBottom || isCaptureBottom
			isFindCaptureHeader = isFindCaptureHeader || isCaptureHeader
			items.add(
				ChatItem.DateSeparator(
					date = date,
					isCaptureBottom = isCaptureBottom,
					isCaptureMiddle = isFindCaptureBottom
									&& isCaptureBottom.not()
									&& isCaptureHeader.not()
									&& isFindCaptureHeader.not(),
					isCaptureHeader = isCaptureHeader,
				)
			)
			continue
		}

		val isSameDate = chat.dispatchTime.isSameDate(this[i + 1].dispatchTime)
		if (isSameDate.not()) {
			val date = chat.dispatchTime.toDateKoreanString()
			val isCaptureBottom = captureBottomItemId == date.hashCode().toLong()
			val isCaptureHeader = captureHeaderItemId == date.hashCode().toLong()
			isFindCaptureBottom = isFindCaptureBottom || isCaptureBottom
			isFindCaptureHeader = isFindCaptureHeader || isCaptureHeader
			items.add(
				ChatItem.DateSeparator(
					date = date,
					isCaptureBottom = isCaptureBottom,
					isCaptureMiddle = isFindCaptureBottom
									&& isCaptureBottom.not()
									&& isCaptureHeader.not()
									&& isFindCaptureHeader.not(),
					isCaptureHeader = isCaptureHeader,
				)
			)
		}
	}

	return items
}