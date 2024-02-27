package com.example.bookchat.domain.repository

import com.example.bookchat.data.SocketMessage
import kotlinx.coroutines.flow.Flow

interface StompHandler {

	suspend fun connectSocket(roomSid: String, roomId: Long): Flow<SocketMessage>
	suspend fun disconnectSocket()

	suspend fun sendMessage(
		roomId: Long,
		message: String
	)

}