package com.example.bookchat.ui.channelList

import com.example.bookchat.domain.model.Channel

data class ChannelListUiState(
	val uiState: UiState,
	val channels: List<Channel>,
) {

	enum class UiState {
		SUCCESS,
		LOADING,
		ERROR,
		EMPTY,
	}

	companion object {
		val DEFAULT = ChannelListUiState(
			uiState = UiState.EMPTY,
			channels = emptyList()
		)
	}
}

sealed class ChannelListUiEvent {
	data class MoveToChannel(val channelId: Long) : ChannelListUiEvent()
	object MoveToMakeChannelPage : ChannelListUiEvent()
	object MoveToSearchChannelPage : ChannelListUiEvent()
}