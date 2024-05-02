package com.example.bookchat.ui.search.channelInfo

import com.example.bookchat.domain.model.Channel

data class ChannelInfoUiState(
	val uiState: UiState,
	val channel: Channel
) {

	enum class UiState {
		SUCCESS,
		LOADING,
		ERROR,
	}

	companion object {
		val DEFAULT = ChannelInfoUiState(
			uiState = UiState.LOADING,
			channel = Channel.DEFAULT
		)
	}
}

sealed class ChannelInfoEvent {
	object MoveToBack : ChannelInfoEvent()
	data class MoveToChannel(val channelId: Long) : ChannelInfoEvent()
}
