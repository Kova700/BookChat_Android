package com.kova700.bookchat.core.stomp.chatting.external.model

enum class SocketState {
	DISCONNECTED,
	CONNECTING,
	CONNECTED,
	FAILURE,
	NEED_RECONNECTION,
}