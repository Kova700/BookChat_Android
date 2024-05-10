package com.example.bookchat.domain.repository

interface StompHandler {

	suspend fun connectSocket(channelSId: String, channelId: Long)
	suspend fun disconnectSocket()

	suspend fun sendMessage(
		channelId: Long,
		message: String
	)

}