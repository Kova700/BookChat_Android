package com.example.bookchat.notification

import com.example.bookchat.domain.model.Channel
import com.example.bookchat.domain.model.Chat

interface NotificationHandler {
	suspend fun showNotification(channel: Channel, chat: Chat)
	fun dismissChannelNotifications(channelId: Long)
	fun dismissAllNotifications()
}