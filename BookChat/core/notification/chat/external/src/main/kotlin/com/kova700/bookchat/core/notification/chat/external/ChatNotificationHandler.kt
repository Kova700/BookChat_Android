package com.kova700.bookchat.core.notification.chat.external

import com.kova700.bookchat.core.data.channel.external.model.Channel
import com.kova700.bookchat.core.data.chat.external.model.Chat

interface ChatNotificationHandler {
	suspend fun showNotification(channel: Channel, chat: Chat)
	suspend fun dismissChannelNotifications(channel: Channel)
	suspend fun dismissAllNotifications()
	suspend fun dismissNotification(notificationId: Int)
}