package com.kova700.bookchat.core.stomp.chatting.external

import com.kova700.bookchat.core.stomp.chatting.external.model.SocketState
import com.kova700.bookchat.core.stomp.chatting.external.model.SubscriptionState
import kotlinx.coroutines.flow.Flow

interface StompHandler {
	val isSocketConnected: Boolean

	fun getSocketStateFlow(): Flow<SocketState>
	fun getChannelSubscriptionStateFlow(channelId: Long): Flow<SubscriptionState>

	suspend fun connectSocket(
		maxAttempts: Int = DEFAULT_RETRY_MAX_ATTEMPTS,
	)

	suspend fun subscribeChannel(
		channelId: Long,
		channelSId: String,
		maxAttempts: Int = DEFAULT_RETRY_MAX_ATTEMPTS
	)

	suspend fun disconnectSocket()
	suspend fun sendMessage(
		channelId: Long,
		message: String,
	)

	suspend fun retrySendMessage(chatId: Long)

	fun isChannelSubscribed(channelId: Long): Boolean

	companion object {
		private const val DEFAULT_RETRY_MAX_ATTEMPTS = 5
	}
}