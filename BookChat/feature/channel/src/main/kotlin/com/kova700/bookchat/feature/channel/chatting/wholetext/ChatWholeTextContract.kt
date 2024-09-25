package com.kova700.bookchat.feature.channel.chatting.wholetext

data class ChatWholeTextUiState(
	val chatMessage: String,
) {
	companion object {
		val DEFAULT = ChatWholeTextUiState(
			chatMessage = ""
		)
	}
}

sealed class ChatWholeTextEvent {
	data object MoveBack : ChatWholeTextEvent()
}