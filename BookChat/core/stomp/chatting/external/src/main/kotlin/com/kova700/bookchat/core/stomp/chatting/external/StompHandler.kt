package com.kova700.bookchat.core.stomp.chatting.external

import com.kova700.bookchat.core.data.channel.external.model.Channel
import com.kova700.bookchat.core.stomp.chatting.external.model.SocketState
import kotlinx.coroutines.flow.StateFlow

interface StompHandler {
	fun getSocketStateFlow(): StateFlow<SocketState>

	suspend fun connectSocket(
		channel: Channel,
		maxAttempts: Int = DEFAULT_RETRY_MAX_ATTEMPTS,
	)

	suspend fun disconnectSocket(channelId : Long)
	suspend fun sendMessage(
		channelId: Long,
		message: String,
	)

	suspend fun retrySendMessage(chatId: Long)

	fun isSocketConnected(channelId: Long): Boolean

	companion object {
		private const val DEFAULT_RETRY_MAX_ATTEMPTS = 5
	}
}