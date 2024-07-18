package com.example.bookchat.notification.chat

import com.example.bookchat.domain.model.Channel
import com.example.bookchat.domain.model.Chat

interface ChatNotificationHandler {
	suspend fun showNotification(channel: Channel, chat: Chat)
	suspend fun dismissChannelNotifications(channel: Channel)
	suspend fun dismissAllNotifications()
	suspend fun dismissNotification(notificationId: Int)
}