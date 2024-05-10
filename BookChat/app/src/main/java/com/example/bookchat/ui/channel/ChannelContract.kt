package com.example.bookchat.ui.channel

import com.example.bookchat.domain.model.Channel
import com.example.bookchat.domain.model.Chat
import com.example.bookchat.ui.channel.model.drawer.ChannelDrawerItem


data class ChannelUiState(
	val uiState: UiState,
	val enteredMessage: String,
	val channel: Channel?,
	val drawerItems: List<ChannelDrawerItem>,
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
			enteredMessage = "",
			channel = null,
			uiState = UiState.EMPTY,
			drawerItems = listOf(ChannelDrawerItem.Header.DEFAULT),
			chats = emptyList()
		)
	}
}

sealed class ChannelEvent {
	object MoveBack : ChannelEvent()
	object CaptureChannel : ChannelEvent()
	object ScrollNewChannelItem : ChannelEvent()
	object OpenOrCloseDrawer : ChannelEvent()
	data class MakeToast(
		val stringId: Int
	) : ChannelEvent()

}
