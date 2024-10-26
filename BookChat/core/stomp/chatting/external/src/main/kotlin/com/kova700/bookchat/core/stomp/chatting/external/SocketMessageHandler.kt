package com.kova700.bookchat.core.stomp.chatting.external

import com.kova700.bookchat.core.stomp.chatting.external.model.SocketMessage

interface SocketMessageHandler {
	suspend fun handleSocketMessage(socketMessage: SocketMessage)
}