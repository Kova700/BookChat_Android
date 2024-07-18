package com.example.bookchat.data.stomp.external.model

enum class SocketState {
	DISCONNECTED,
	CONNECTING,
	CONNECTED,
	FAILURE,
	NEED_RECONNECTION,
}