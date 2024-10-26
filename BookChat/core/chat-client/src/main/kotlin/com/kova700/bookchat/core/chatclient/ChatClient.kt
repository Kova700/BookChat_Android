package com.kova700.bookchat.core.chatclient

import androidx.lifecycle.DefaultLifecycleObserver
import com.kova700.bookchat.core.stomp.chatting.external.model.SocketState
import com.kova700.bookchat.core.stomp.chatting.external.model.SubscriptionState
import kotlinx.coroutines.flow.Flow

interface ChatClient : DefaultLifecycleObserver {
	fun isChannelSubscribed(channelId: Long): Boolean
	fun getSocketStateFlow(): Flow<SocketState>
	fun getChannelSubscriptionStateFlow(channelId: Long): Flow<SubscriptionState>
	fun connectSocketIfNeeded()
	fun subscribeChannelIfNeeded(
		channelId: Long,
		channelSId: String,
	)

	fun sendMessage(
		channelId: Long,
		message: String,
	)

	fun retrySendMessage(chatId: Long)

	suspend fun logout()
	suspend fun withdraw()
}