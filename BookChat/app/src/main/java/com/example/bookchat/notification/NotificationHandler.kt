package com.example.bookchat.notification

import com.example.bookchat.domain.model.Channel
import com.example.bookchat.domain.model.Chat

interface NotificationHandler {
	suspend fun showNotification(channel: Channel, chat: Chat)
	suspend fun dismissChannelNotifications(channel: Channel)
	suspend fun dismissAllNotifications()
	suspend fun dismissNotification(notificationId: Int)
}