package com.example.bookchat.domain.repository

import com.example.bookchat.domain.model.Channel
import com.example.bookchat.domain.model.SocketState
import kotlinx.coroutines.flow.StateFlow

interface StompHandler {
	fun getSocketStateFlow(): StateFlow<SocketState>

	suspend fun connectSocket(
		channel: Channel,
		maxAttempts: Int = DEFAULT_RETRY_MAX_ATTEMPTS,
	)

	suspend fun disconnectSocket()
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