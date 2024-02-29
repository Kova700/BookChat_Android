package com.example.bookchat.ui.viewmodel.contract

import com.example.bookchat.domain.model.Channel
import com.example.bookchat.domain.model.Chat


data class ChannelUiState(
	val inputtedMessage: String,
	val channel: Channel,
	val uiState :UiState,
	val chats: List<Chat>,
) {

	enum class UiState {
		SUCCESS,
		LOADING,
		ERROR,
		EMPTY,
	}

	companion object {
		val DEFAULT = ChannelUiState(
			inputtedMessage = "",
			channel = Channel.DEFAULT,
			uiState = UiState.EMPTY,
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
