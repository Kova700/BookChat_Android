package com.example.bookchat.ui.channelList

import com.example.bookchat.ui.channelList.model.ChannelListItem

data class ChannelListUiState(
	val uiState: UiState,
	val channelListItem: List<ChannelListItem>,
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
			channelListItem = listOf(ChannelListItem.Header)
		)
	}
}

sealed class ChannelListUiEvent {
	data class MoveToChannel(val channelId: Long) : ChannelListUiEvent()
	object MoveToMakeChannelPage : ChannelListUiEvent()
	object MoveToSearchChannelPage : ChannelListUiEvent()
}