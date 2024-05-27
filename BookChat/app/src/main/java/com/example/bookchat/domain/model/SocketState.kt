package com.example.bookchat.domain.model

enum class SocketState {
	DISCONNECTED,
	CONNECTING,
	CONNECTED,
	FAILURE,
	NEED_RECONNECTION
}