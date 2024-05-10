package com.example.bookchat.domain.repository

import com.example.bookchat.data.network.model.response.SocketMessage
import kotlinx.coroutines.flow.Flow

interface StompHandler {

	suspend fun connectSocket(channelSId: String, channelId: Long): Flow<SocketMessage>
	suspend fun disconnectSocket()

	suspend fun sendMessage(
		channelId: Long,
		message: String
	)

}