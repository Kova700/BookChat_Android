package com.example.bookchat.ui.viewmodel.contract

import com.example.bookchat.domain.model.Channel
import com.example.bookchat.domain.model.Chat


data class ChannelUiState(
	val inputtedMessage: String,
	val channel: Channel,
	val chats: List<Chat>,
) {
	companion object {
		val DEFAULT = ChannelUiState(
			inputtedMessage = "",
			channel = Channel.DEFAULT,
			chats = emptyList()
		)
	}
}

sealed class ChannelEvent {
	object MoveBack : ChannelEvent()
	object CaptureChannel : ChannelEvent()
	object ScrollNewChannelItem : ChannelEvent()
	object OpenOrCloseDrawer : ChannelEvent()
}
